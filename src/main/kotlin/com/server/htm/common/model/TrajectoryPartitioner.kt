package com.server.htm.common.model

import com.server.htm.db.dao.TravelSegment
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import kotlin.math.*

class TrajectoryPartitioner(
    val travelId: String,
    originLineStr: LineString
) {
    private val geometryFactory = GeometryFactory()

    var originCoords = originLineStr.coordinates.toList()

    fun partition(): List<TravelSegment>{
        var start = 0
        var length = 1

        //critical points
        val cps = mutableListOf<Int>(start)

        //Approximate Trajectory Partitioning
        while(start + length < originCoords.size){
            val curr = start + length
            val cost_par = MDL_par(start, curr)
            val cost_nopar = MDL_nopar(start, curr)

            if(cost_par > cost_nopar * 0.15){
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

    fun LineFromIdxs(sIdx: Int, eIdx: Int) = Line(this.originCoords[sIdx], this.originCoords[eIdx])

    fun L_H(sIdx: Int, eIdx: Int): Double =
        LineFromIdxs(sIdx, eIdx).length()


    fun L_DH(sIdx: Int, eIdx: Int): Double{
        val line1 = LineFromIdxs(sIdx, eIdx)

        var perpendicularDistSum = 0.0
        var angularDistSum = 0.0

        for(i in sIdx..eIdx-1){
            val line2 = LineFromIdxs(i, i+1)
            perpendicularDistSum += Line.perpendicularDistance(line1, line2)
            angularDistSum += Line.angleDistance(line1, line2)
        }

        return log2(perpendicularDistSum) + log2(angularDistSum)
    }

    fun MDL_par(sIdx: Int, eIdx: Int): Double = 1.0 * L_H(sIdx, eIdx) + 8.0 * L_DH(sIdx, eIdx)

    fun MDL_nopar(sIdx: Int, eIdx: Int): Double{
        var sum = 0.0
        for(i in sIdx..eIdx-1){
            sum += L_H(i, i+1)
        }
        return sum
    }
}