package com.server.htm.common.model

import com.server.htm.common.dirVector
import com.server.htm.common.haversineDistance
import com.server.htm.common.theta
import com.server.htm.common.thetaDegree
import com.server.htm.db.dao.CustomConfigs
import com.server.htm.db.dao.TravelSegment
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import kotlin.math.*

class TrajectoryPartitioner(
    val travelId: String,
    originLineStr: LineString
) {
    private val geometryFactory = GeometryFactory()

    var originCoords = originLineStr.coordinates.toList()

    fun partition(configs: CustomConfigs): List<TravelSegment>{
        val angleDiff = configs.getJsonValue("angleDiff")
            ?: throw Exception("AngleDiff must be specified")

        var start = 0
        var length = 1

        //critical points
        val cps = mutableListOf<Int>(start)

        //Approximate Trajectory Partitioning
        while(start + length < originCoords.size){
            val curr = start + length

            if(length == 1) {
                length++
                continue
            }

            val avgAngle = avgAngle(start, curr-1)
            val dirAngle = thetaDegree(originCoords[curr-1], originCoords[curr])

            if(abs(avgAngle - dirAngle) > angleDiff) {
                cps.add(curr-1)
                start = curr-1
                length = 1
            } else {
                length++
            }
        }
        cps.add(originCoords.lastIndex)

        val result = mutableListOf<TravelSegment>()
        for(i in 0..cps.size-2){
            val lineLength = haversineDistance(originCoords[cps[i]], originCoords[cps[i+1]])
            if(lineLength < 1) continue

            result.add(
                TravelSegment(
                    travelId = travelId,
                    path = geometryFactory.createLineString(originCoords.subList(cps[i], cps[i+1]+1).toTypedArray()),
                    convenienceLevel = "0",
                    lineSegment = geometryFactory.createLineString(arrayOf(originCoords[cps[i]], originCoords[cps[i+1]])),
                    idx = i
                )
            )
        }
        return result
    }

    fun avgAngle(sIdx: Int, eIdx: Int): Double {
        var sum = 0.0
        for(i in sIdx..eIdx-2){
            val vector = dirVector(originCoords[sIdx], originCoords[i+1])
            sum += thetaDegree(Coordinate(0.0,0.0), vector)
        }
        return sum / (eIdx-sIdx-1)
    }
}