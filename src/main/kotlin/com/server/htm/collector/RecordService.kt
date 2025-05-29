package com.server.htm.collector

import com.server.htm.collector.dto.GetRawPathsRes
import com.server.htm.collector.dto.PostDataReq
import com.server.htm.collector.dto.StartRecordReq
import com.server.htm.common.dto.Response
import com.server.htm.common.enum.ActivityType
import com.server.htm.common.enum.RecordStatus
import com.server.htm.db.dao.Travel
import com.server.htm.db.dao.TravelData
import com.server.htm.db.dao.TravelGpsPath
import com.server.htm.db.repo.TravelDataRepository
import com.server.htm.db.repo.TravelGpsPathRepository
import com.server.htm.db.repo.TravelRepository
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.*

@Service
class RecordService(
    private val travelRepo: TravelRepository,
    private val travelDataRepo: TravelDataRepository,
    private val travelGpsPathRepo: TravelGpsPathRepository

) {
    private val geometryFactory = GeometryFactory()

    fun startRecord(req: StartRecordReq): Response {
        if(travelRepo.existsById(req.travelId))
            return Response("Travel Already Exists")

        travelRepo.save(
            Travel(
                id = req.travelId,
                startTime = OffsetDateTime.now(),
                status = RecordStatus.RECORDING
            )
        )

        return Response()
    }

    fun postData(
        req: PostDataReq
    ): Response {
        val travel = travelRepo.findByIdOrNull(req.travelId) ?: return Response("No such travel")

        travelDataRepo.save(
            TravelData(
                travelId = travel.id,
                gps = geometryFactory.createPoint(Coordinate(req.lng, req.lat)),
                accuracy = req.accuracy,
                activity = ActivityType.valueOf(req.activityType)
            )
        )

        return Response()
    }

    fun endRecord(travelId: String): Response {
        val travel = travelRepo.findByIdOrNull(travelId) ?: return Response("No such travel")
        travel.endTime = OffsetDateTime.now()
        travel.status = RecordStatus.DONE

        travelRepo.save(travel)

        val datas = travelDataRepo.findAllByTravelIdOrderByTimestamp(travel.id)
        val rawPath = geometryFactory.createLineString(datas.mapNotNull { it.gps.coordinate }.toTypedArray())
        val filteredPath = datas.filteredLineStr()

        travelGpsPathRepo.save(
            TravelGpsPath(
                travelId = travel.id,
                rawPath = rawPath,
                filteredPath = filteredPath
            )
        )

        return Response()
    }

    fun filterAllRecord(): Response{
        val travels = travelRepo.findAllByStatus(RecordStatus.DONE)
        travels.forEach {
            val travelGpsPath = travelGpsPathRepo.findTopByTravelId(it.id) ?: return@forEach
            val datas = travelDataRepo.findAllByTravelIdOrderByTimestamp(it.id)
            travelGpsPath.filteredPath = datas.filteredLineStr()
            travelGpsPathRepo.save(travelGpsPath)
        }

        return Response()
    }

    fun getRawPath(
    ): GetRawPathsRes{
        return GetRawPathsRes(
            paths = travelGpsPathRepo.findAll().map {
                it.rawPath.coordinates
                    .map { listOf(it.x, it.y) }
                    .fold(mutableListOf()) { acc, current ->
                        if (acc.isEmpty() || (acc.last()[0] != current[0] && acc.last()[0] != current[0])) {
                            acc.add(current)
                        }
                        acc
                    }
            }
        )
    }

    fun getFilteredPath(
    ): GetRawPathsRes{
        return GetRawPathsRes(
            paths = travelGpsPathRepo.findAll().mapNotNull {
                it.filteredPath?.coordinates
                    ?.map { listOf(it.x, it.y) }
                    ?.fold(mutableListOf()) { acc, current ->
                        if (acc.isEmpty() || (acc.last()[0] != current[0] && acc.last()[0] != current[0])) {
                            acc.add(current)
                        }
                        acc
                    }
            }
        )
    }

    fun List<TravelData>.filteredLineStr(): LineString{
        if(this.isEmpty()) return emptyLineString()
        val unDuplicatedList = this.unDuplicated()

//        // 2. 꺾이는 각도 기반 분할
//        fun angleBetween(p1: Point, p2: Point, p3: Point): Double {
//            val v1x = p1.x - p2.x
//            val v1y = p1.y - p2.y
//            val v2x = p3.x - p2.x
//            val v2y = p3.y - p2.y
//            val angle1 = atan2(v1y, v1x)
//            val angle2 = atan2(v2y, v2x)
//            val diff = Math.abs(Math.toDegrees(angle2 - angle1))
//            return if (diff > 180) 360 - diff else diff
//        }
//
//        val chunks = mutableListOf<MutableList<TravelData>>()
//        var currentChunk = mutableListOf<TravelData>()
//        currentChunk.add(unDuplicatedList[0])
//
//        for (i in 1 until unDuplicatedList.size - 1) {
//            currentChunk.add(unDuplicatedList[i])
//            val angle = angleBetween(
//                unDuplicatedList[i - 1].gps,
//                unDuplicatedList[i].gps,
//                unDuplicatedList[i + 1].gps
//            )
//            if (angle >= 100.0) {
//                chunks.add(currentChunk)
//                currentChunk = mutableListOf()
//            }
//        }
//        currentChunk.add(unDuplicatedList.last())
//        chunks.add(currentChunk)
//
//        // 3. 각 chunk에 Kalman 적용 후 병합
//        return chunks
//            .map { chunk -> kalmanFiltering(chunk) }
//            .reduce { l1, l2 -> mergeLineStrings(l1, l2) }


        return unDuplicatedList.chunked(min(20,max(1,this.size / 10)))
            .map { chunk -> kalmanFiltering(chunk) }
            .reduce { l1, l2 ->  mergeLineStrings(l1,l2)}
    }

    fun List<TravelData>.unDuplicated(): MutableList<TravelData> {
        val unDuplicatedList = mutableListOf<TravelData>(this[0])
        var isContinue = true
        for(i in 0  ..this.size-2){
            if(this[i].gps != this[i+1].gps){
                if(isContinue) unDuplicatedList.add(this[i+1])
                else {
                    unDuplicatedList.add(this[i])
                    unDuplicatedList.add(this[i+1])
                    isContinue = true
                }
            } else {
                if(isContinue) {
                    isContinue = false
                }
            }
        }
        return unDuplicatedList
    }

    fun mergeLineStrings(line1: LineString, line2: LineString): LineString {
        val coordinates1 = line1.coordinates
        val coordinates2 = line2.coordinates

        // 두 LineString의 좌표를 하나의 배열로 합침
        val combinedCoordinates = coordinates1 + coordinates2

        // GeometryFactory를 사용하여 새로운 LineString 생성
        val geometryFactory = GeometryFactory()
        return geometryFactory.createLineString(combinedCoordinates)
    }

    fun kalmanFiltering(travelDataList: List<TravelData>): LineString {
        var filteredCoords = mutableListOf<Coordinate>()

        var lastTD: TravelData = travelDataList.firstOrNull() ?: return emptyLineString()

        val lastCoord = Coordinate(0.0, 0.0)

        val qMetersPerSecond = 5
        var variance = -1.0 // P matrix. Negative means uninitialized

        travelDataList.forEach { td ->
            val accuracy = if(td.accuracy < 0) 10.0 else td.accuracy
            if(variance < 0) {
                variance = accuracy.times(accuracy)
                lastCoord.x = td.gps.x
                lastCoord.y = td.gps.y
                filteredCoords.add(lastCoord.copy())
                return@forEach
            }

            val timeInc = (td.timestamp.nano - lastTD.timestamp.nano) / 1000000000.0

            variance += timeInc * qMetersPerSecond * qMetersPerSecond

            // Kalman gain
            val k = variance / (variance + accuracy * accuracy)

            // 보정
            lastCoord.x = lastCoord.x + k * (td.gps.x - lastCoord.x)
            lastCoord.y = lastCoord.y + k * (td.gps.y - lastCoord.y)

            lastTD = td
            variance *= (1 - k)

            filteredCoords.add(lastCoord.copy())
        }

        return if(filteredCoords.size < 2) emptyLineString()
            else geometryFactory.createLineString(filteredCoords.toTypedArray())
    }

    fun emptyLineString(): LineString = geometryFactory.createLineString(emptyArray())
}