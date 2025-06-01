package com.server.htm.view

import com.server.htm.db.repo.RelaxZoneRepository
import com.server.htm.db.repo.TravelClusterRepository
import com.server.htm.db.repo.TravelGpsPathRepository
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.stereotype.Service

@Service
class ViewService(
    private val travelGpsPathRepo: TravelGpsPathRepository,
    private val relaxZoneRepo: RelaxZoneRepository,
    private val travelClusterRepo: TravelClusterRepository,
) {
    private val geometryFactory = GeometryFactory()

    fun getRawPath(
    ): GetGeomRes{
        return GetGeomRes(
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
    ): GetGeomRes{
        return GetGeomRes(
            paths = travelGpsPathRepo.findAll().mapNotNull {
                it.segmentPath?.coordinates
                    ?.map { listOf(it.x, it.y) }
            }
        )
    }

    fun getRelaxZones(
    ): GetGeomRes{
        return GetGeomRes(
            paths = relaxZoneRepo.findAll().mapNotNull {
                it.area.exteriorRing.coordinates
                    .map { listOf(it.x, it.y) }
            }
        )
    }
    fun getCluster(
    ): GetGeomRes{
        return GetGeomRes(
            paths = travelClusterRepo.findAll().mapNotNull {
                it.path.coordinates
                    .map { listOf(it.x, it.y) }
            }
        )
    }


}