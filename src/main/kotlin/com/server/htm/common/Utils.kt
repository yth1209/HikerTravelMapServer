package com.server.htm.common

import org.locationtech.jts.geom.Coordinate
import java.util.UUID
import kotlin.math.*

fun uuid() = UUID.randomUUID().toString().replace("-", "")

fun haversineDistance(point1: Coordinate, point2: Coordinate): Double =
    haversineDistance(point1.y, point1.x, point2.y, point2.x)

fun dirVector(s: Coordinate, e: Coordinate) = Coordinate(e.x - s.x, e.y - s.y)

fun theta(s: Coordinate, e: Coordinate): Double = atan2(e.y-s.y, e.x-s.x)

fun thetaDegree(s: Coordinate, e: Coordinate): Double = Math.toDegrees(theta(s,e))


fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0 // 지구 반지름 (미터)

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c
}