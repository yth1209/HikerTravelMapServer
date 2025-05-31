package com.server.htm.traclus

import com.server.htm.common.dto.Response
import com.server.htm.common.enum.RecordStatus
import com.server.htm.common.model.TrajectoryPartitioner
import com.server.htm.db.dao.RelaxZone
import com.server.htm.db.dao.Travel
import com.server.htm.db.dao.TravelRelaxZone
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
    private val relaxZoneRepo: RelaxZoneRepository
) {
    private val geometryFactory = GeometryFactory()

    fun partitionAll(
    ): Response {
        travelSegmentRepo.deleteAll()
        travelGpsPathRepo.findAll()
            .forEach { travelGpsPath ->
                travelGpsPath.filteredPath?.let { path ->
                    if(path.isEmpty) return@forEach
                    val travelSegments = TrajectoryPartitioner(travelGpsPath.travelId, path).partition()
                    if(travelSegments.isEmpty()) return@forEach

                    travelSegmentRepo.saveAll(travelSegments)


                    travelGpsPath.segmentPath = geometryFactory.createLineString(
                        travelSegments.map { it.lineSegment.coordinates }.reduce { a, b -> a + b }
                    )
                    travelGpsPathRepo.save(travelGpsPath)
                }
            }
        return Response("OK")
    }

    @Transactional
    fun globalRelaxZone(): Response {
        relaxZoneRepo.deleteAll()
        relaxZoneRepo.flush()

        val travels = travelRepo.findAllByStatus(RecordStatus.DONE)


        travels.forEach {
            mergeToGlobalRelaxZone(it)
        }

        return Response("OK")
    }

    fun mergeToGlobalRelaxZone(travel: Travel){
        val tRelaxZones = travelRelaxZoneRepo.findAllByTravelIdAndAreaIsNotNull(travel.id)

        tRelaxZones.forEach {
            if(it.area == null) return@forEach

            val nearRelaxZone = relaxZoneRepo.findTopNearbyPolygon(it.area!!)
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