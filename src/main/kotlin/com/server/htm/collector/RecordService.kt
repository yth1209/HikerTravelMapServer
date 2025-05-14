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
import java.time.OffsetDateTime

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
        val filteredPath = kalmanFiltering(datas)

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
            travelGpsPath.filteredPath = kalmanFiltering(datas)
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

    fun kalmanFiltering(travelDataList: List<TravelData>): LineString {
        var filteredCoords = mutableListOf<Coordinate>()

        var lastTD: TravelData = travelDataList.firstOrNull() ?: return geometryFactory.createLineString(filteredCoords.toTypedArray())

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

        return geometryFactory.createLineString(filteredCoords.toTypedArray())
    }
}