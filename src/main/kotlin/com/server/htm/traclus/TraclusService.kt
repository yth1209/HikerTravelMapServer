package com.server.htm.traclus

import com.server.htm.common.dto.Response
import com.server.htm.common.model.TrajectoryPartitioner
import com.server.htm.db.repo.TravelDataRepository
import com.server.htm.db.repo.TravelGpsPathRepository
import com.server.htm.db.repo.TravelRepository
import com.server.htm.db.repo.TravelSegmentRepository
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.stereotype.Service

@Service
class TraclusService(
    private val travelRepo: TravelRepository,
    private val travelDataRepo: TravelDataRepository,
    private val travelGpsPathRepo: TravelGpsPathRepository,
    private val travelSegmentRepo: TravelSegmentRepository,

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

}