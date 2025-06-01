package com.server.htm.common.model

import com.server.htm.common.haversineDistance
import org.locationtech.jts.geom.Coordinate

class RelaxZoneCluster(
    val points: MutableList<Coordinate>,
    val cnt: Int,
) {

    fun isNearBy(cluster: RelaxZoneCluster): Boolean {
        return points.find { cluster.isNearBy(it) } != null
    }

    fun isNearBy(point: Coordinate): Boolean {
        return this.points.find {
            val dist = haversineDistance(it, point)
            dist < 0.8
        } != null
    }


    fun merge(clusters: List<RelaxZoneCluster>): RelaxZoneCluster{
        val newPoints = mutableListOf<Coordinate>()
        var newCnt = this.cnt
        newPoints.addAll(points)
        clusters.forEach {
            newPoints.addAll(it.points)
            newCnt += it.cnt
        }

        return RelaxZoneCluster(
            newPoints,
            newCnt
        )
    }
}