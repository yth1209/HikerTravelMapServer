package com.server.htm.traclus

import com.server.htm.common.dto.Response
import com.server.htm.common.enum.RecordStatus
import com.server.htm.common.model.GPSFilter
import com.server.htm.common.model.RelaxZoneCluster
import com.server.htm.common.model.TrajectoryPartitioner
import com.server.htm.db.dao.ConfigTypes
import com.server.htm.db.dao.RelaxZone
import com.server.htm.db.dao.Travel
import com.server.htm.db.dao.TravelGpsPath
import com.server.htm.db.dao.TravelRelaxZone
import com.server.htm.db.repo.CustomConfigsRepository
import com.server.htm.db.repo.RelaxZoneRepository
import com.server.htm.db.repo.TravelDataRepository
import com.server.htm.db.repo.TravelGpsPathRepository
import com.server.htm.db.repo.TravelRelaxZoneRepository
import com.server.htm.db.repo.TravelRepository
import com.server.htm.db.repo.TravelSegmentRepository
import jakarta.transaction.Transactional
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import org.springframework.stereotype.Service

@Service
class TraclusService(
    private val travelRepo: TravelRepository,
    private val travelDataRepo: TravelDataRepository,
    private val travelGpsPathRepo: TravelGpsPathRepository,
    private val travelSegmentRepo: TravelSegmentRepository,
    private val travelRelaxZoneRepo: TravelRelaxZoneRepository,
    private val relaxZoneRepo: RelaxZoneRepository,
    private val customConfigsRepo: CustomConfigsRepository
) {
    private val geometryFactory = GeometryFactory()

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

    fun partitionAll(
    ): Response {
        travelSegmentRepo.deleteAll()

        travelGpsPathRepo.findAll()
            .forEach { travelGpsPath ->
                travelGpsPath.filteredPath?.let { path ->
                    partition(travelGpsPath)
                }
            }
        return Response("OK")
    }

    fun partition(travelGpsPath: TravelGpsPath) {
        val travelSegmentConfig = customConfigsRepo.findTopByType(ConfigTypes.TRAVEL_SEGMENTATION)
            ?: throw Exception("No such travel segments configs")

        travelGpsPath.filteredPath?.let { path ->
            if(path.isEmpty) return

            val travelSegments = TrajectoryPartitioner(travelGpsPath.travelId, path).partition(travelSegmentConfig)
            if(travelSegments.isEmpty()) return

            travelSegmentRepo.saveAll(travelSegments)


            travelGpsPath.segmentPath = geometryFactory.createLineString(
                travelSegments.map { it.lineSegment.coordinates }.reduce { a, b -> a + b }
            )
            travelGpsPathRepo.save(travelGpsPath)
        }
    }

    @Transactional
    fun createGlobalRelaxZoneAll(): Response {
        relaxZoneRepo.deleteAll()
        relaxZoneRepo.flush()

        val travels = travelRepo.findAllByStatus(RecordStatus.DONE)


        travels.forEach {
            mergeToGlobalRelaxZone(it)
        }

        return Response("OK")
    }

    fun saveTravelRelaxZone(gpsFilter: GPSFilter, travelId: String){
        travelRelaxZoneRepo.deleteAllByTravelId(travelId)

        val clusters = mutableListOf<RelaxZoneCluster>()

        val unvisitedCluster = gpsFilter.duplicatedMap
            .map { (key, value) ->
                RelaxZoneCluster(mutableListOf(key), value)
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
                .filter { it.cnt > 20 }
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

    fun mergeToGlobalRelaxZone(travel: Travel){
        val configs = customConfigsRepo.findTopByType(ConfigTypes.RELAX_ZONE) ?:  return
        val distance = configs.getJsonValue("nearDistance") ?: return
        val tRelaxZones = travelRelaxZoneRepo.findAllByTravelIdAndAreaIsNotNull(travel.id)

        tRelaxZones.forEach {
            if(it.area == null) return@forEach
            it.area!!.srid = 4326
            val nearRelaxZone = relaxZoneRepo.findTopNearbyPolygon(it.area!!, distance)
            if(nearRelaxZone == null) {
                relaxZoneRepo.save(
                    RelaxZone(
                        area = it.area!!,
                        cnt = it.cnt
                    )
                )
                relaxZoneRepo.flush()
            } else {
                val collection = geometryFactory.createGeometryCollection(arrayOf(it.area!!, nearRelaxZone.area))
                val hull = ConvexHull(collection).convexHull
                if(hull is Polygon) {
                    nearRelaxZone.area = hull
                    nearRelaxZone.cnt += it.cnt
                    relaxZoneRepo.save(nearRelaxZone)
                    relaxZoneRepo.flush()
                }
            }
        }
    }
}