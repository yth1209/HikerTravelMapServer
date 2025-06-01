package com.server.htm.common.model

import com.server.htm.common.*
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import kotlin.math.*

class Line(
    var s: Coordinate,
    var e: Coordinate
) {
    private val geometryFactory = GeometryFactory()

    fun theta() = theta(s, e)
    fun thetaDegree() = thetaDegree(s, e)

    fun length(): Double = lineLength(s,e)

    private fun lineLength(s: Coordinate, e: Coordinate): Double = sqrt((s.x - e.x).pow(2) + (s.y - e.y).pow(2))


    fun mergeLine(line: Line) {
        if(lineLength(s, e) < lineLength(s, line.e)){
            this.e = line.e.copy()
        }
        if(lineLength(s, e) < lineLength(line.s, e)){
            this.s = line.s.copy()
        }
    }

    //return a,b,c s.t ax+by+c=0 through this line
    fun linearEquation(): Triple<Double, Double, Double>{
        val a = e.y - s.y
        val b = s.x - e.x
        val c = s.y*e.x - e.y*s.x
        return Triple(a,b,c)
    }

    fun perpendicularDistance(point: Coordinate): Double {
        val projectionP = projectionCoordinate(point)

        return haversineDistance(projectionP, point)
    }

    fun parallelDistance(point: Coordinate): Double {
        val projectionP = projectionCoordinate(point)
        return min(lineLength(projectionP, this.s), lineLength(projectionP, this.e))
    }

    fun angleDistance(point: Coordinate): Double {
        val projectionP = projectionCoordinate(point)
        return min(lineLength(projectionP, this.s), lineLength(projectionP, this.e))
    }

    // 주어진 점에서 이 직선에 내린 수선의 발 반환
    fun projectionCoordinate(point: Coordinate): Coordinate {
        val (a, b, c) = this.linearEquation()
        val denom = a * a + b * b
        if (denom == 0.0) return s // 선이 잘못 정의된 경우 (점 하나)

        val x = (b * (b * point.x - a * point.y) - a * c) / denom
        val y = (a * (-b * point.x + a * point.y) - b * c) / denom

        return Coordinate(x,y)
    }

    fun isOverlappedPoint(point: Coordinate): Boolean {
        val minX = min(s.x, e.x)
        val maxX = max(s.x, e.x)
        val minY = min(s.y, e.y)
        val maxY = max(s.y, e.y)

        return point.x in minX..maxX && point.y in minY..maxY
    }

    fun isOverlappedLine(line: Line): Boolean {
        val p1 = this.projectionCoordinate(line.s)
        val p2 = this.projectionCoordinate(line.e)

        return isOverlappedPoint(p1) || isOverlappedPoint(p2)
    }

    fun perpendicularDistance(line: Line): Double {
        val l1 = line.perpendicularDistance(this.s)
        val l2 = line.perpendicularDistance(this.e)
        return (l1.pow(2)+l2.pow(2))/(l1 + l2)
    }

    fun parallelDistance(line: Line): Double {
        val l1 = line.parallelDistance(this.s)
        val l2 = line.parallelDistance(this.e)

        return min(l1, l2)
    }

    fun angleDistance(line: Line): Double {
        val theta = abs(this.theta() -line.theta()) % PI
        return if(0 <= this.theta() && line.theta() < PI/2){
            this.length() * sin(theta)
        } else {
            this.length()
        }
    }

    fun copy(): Line {
        return Line(s,e)
    }
}