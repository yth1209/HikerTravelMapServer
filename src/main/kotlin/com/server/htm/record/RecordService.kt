package com.server.htm.record

import com.server.htm.record.dto.GetRawPathsRes
import com.server.htm.record.dto.PostDataReq
import com.server.htm.record.dto.StartRecordReq
import com.server.htm.common.model.GPSFilter
import com.server.htm.common.dto.Response
import com.server.htm.common.enum.ActivityType
import com.server.htm.common.enum.RecordStatus
import com.server.htm.common.model.RelaxZoneCluster
import com.server.htm.db.dao.Travel
import com.server.htm.db.dao.TravelData
import com.server.htm.db.dao.TravelGpsPath
import com.server.htm.db.dao.TravelRelaxZone
import com.server.htm.db.repo.TravelDataRepository
import com.server.htm.db.repo.TravelGpsPathRepository
import com.server.htm.db.repo.TravelRelaxZoneRepository
import com.server.htm.db.repo.TravelRepository
import jakarta.transaction.Transactional
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import kotlin.collections.component1
import kotlin.collections.component2

@Service
class RecordService(
    private val travelRepo: TravelRepository,
    private val travelDataRepo: TravelDataRepository,
    private val travelGpsPathRepo: TravelGpsPathRepository,
    private val travelRelaxZoneRepo: TravelRelaxZoneRepository,

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

        val gpsFilter = GPSFilter(datas)
        val filteredPath = gpsFilter.filteredLineStr
        travelGpsPathRepo.save(
            TravelGpsPath(
                travelId = travel.id,
                rawPath = rawPath,
                filteredPath = filteredPath
            )
        )
        saveTravelRelaxZone(gpsFilter, travel.id)

        return Response()
    }

    @Transactional
    fun filterAllRecord(): Response{
        val travels = travelRepo.findAllByStatus(RecordStatus.DONE)
        travels.forEach {
            val travelGpsPath = travelGpsPathRepo.findTopByTravelId(it.id) ?: return@forEach
            val datas = travelDataRepo.findAllByTravelIdOrderByTimestamp(it.id)

            val gpsFilter = GPSFilter(datas)
            travelGpsPath.filteredPath = gpsFilter.filteredLineStr
            travelGpsPathRepo.save(travelGpsPath)
            saveTravelRelaxZone(gpsFilter, it.id)
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

    fun saveTravelRelaxZone(gpsFilter: GPSFilter, travelId: String){
        travelRelaxZoneRepo.deleteAllByTravelId(travelId)

        val clusters = mutableListOf<RelaxZoneCluster>()


        val unvisitedCluster = gpsFilter.duplicatedMap
            .filter { (key, value) -> value > 4 }
            .map {
                (key, value) -> RelaxZoneCluster(mutableListOf(key), value)
            }.toMutableList()

        while(unvisitedCluster.isNotEmpty()){
            val visit = unvisitedCluster.removeAt(0)

            val nearClusters = unvisitedCluster.filter { it.isNearBy(visit) }

            if(nearClusters.isEmpty()){
                clusters.add(visit)
                continue
            }

            val newCluster = visit.merge(nearClusters)

            unvisitedCluster.removeAll(nearClusters)
            unvisitedCluster.add(newCluster)
        }


        travelRelaxZoneRepo.saveAll(
            clusters
                .filter {
                    it.cnt > 20
                }
                .map { cluster ->
                    val points = geometryFactory.createMultiPoint(
                        cluster.points.map {
                            geometryFactory.createPoint(it)
                        }.toTypedArray()
                    )

                    TravelRelaxZone(
                        travelId = travelId,
                        points = points,
                        area = ConvexHull(points).convexHull as? Polygon,
                        type = "0",
                        cnt = cluster.cnt
                    )
                }
        )
    }

}