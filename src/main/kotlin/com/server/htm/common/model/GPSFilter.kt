package com.server.htm.common.model

import com.server.htm.db.dao.TravelData
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import kotlin.math.*

class GPSFilter(
    val data: List<TravelData>
) {
    private val geometryFactory = GeometryFactory()
    private val unDuplicatedData = mutableListOf<TravelData>()

    var filteredLineStr: LineString = emptyLineString()
    val duplicatedMap = mutableMapOf<Coordinate, Int>()


    init {
        this.filterDuplicatedData()
        this.filteredLineStr()
    }

    fun filteredLineStr() {
        if(unDuplicatedData.isEmpty()) return

        filteredLineStr = unDuplicatedData.chunked(min(20, max(1, data.size / 10)))
            .map { chunk -> kalmanFiltering(chunk) }
            .reduce { l1, l2 ->  mergeLineStrings(l1,l2)}
    }

    fun filterDuplicatedData() {
        unDuplicatedData.add(data[0])
        duplicatedMap.add(Coordinate(data[0].gps.x, data[0].gps.y))

        var isContinue = true
        for(i in 0  ..data.size-2){
            if(data[i].gps != data[i+1].gps){
                if(isContinue) unDuplicatedData.add(data[i+1])
                else {
                    unDuplicatedData.add(data[i])
                    unDuplicatedData.add(data[i+1])
                    isContinue = true
                }
            } else {
                if(isContinue) {
                    isContinue = false
                }
            }
            duplicatedMap.add(Coordinate(data[i+1].gps.x, data[i+1].gps.y))
        }
    }

    fun MutableMap<Coordinate, Int>.add(point: Coordinate){
        if(this[point] == null) this[point] = 1
        else this[point] = this[point]!! + 1
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
                variance = accuracy * accuracy
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