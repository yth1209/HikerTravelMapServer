package com.server.htm.common.model

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import kotlin.math.*

class Line(
    val s: Coordinate,
    val e: Coordinate
) {
    private val geometryFactory = GeometryFactory()

    fun length(): Double = lineLength(s,e)

    fun lineLength(s: Coordinate, e: Coordinate): Double = sqrt((s.x - e.x).pow(2) + (s.y - e.y).pow(2))

    //return a,b,c s.t ax+by+c=0 through this line
    fun linearEquation(): Triple<Double, Double, Double>{
        val a = e.y - s.y
        val b = s.x - e.x
        val c = s.y*e.x - e.y*s.x
        return Triple(a,b,c)
    }

    fun perpendicularDistance(point: Coordinate): Double {
        val (a,b,c) = this.linearEquation()
        val denom = a * a + b * b
        if (denom == 0.0)
            return lineLength(point, this.s)

        return abs(a*point.x + b*point.y + c)
            .div(sqrt(a.pow(2)+b.pow(2)))
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

    fun perpendicularDistance(line1: Line, line2: Line): Double {
        val l1 = line2.perpendicularDistance(line1.s)
        val l2 = line2.perpendicularDistance(line1.e)
        return (l1.pow(2)+l2.pow(2))/(l1 + l2)
    }

    fun parallelDistance(line1: Line, line2: Line): Double {
        val l1 = line2.parallelDistance(line1.s)
        val l2 = line2.parallelDistance(line1.e)

        return min(l1, l2)
    }

    fun angleDistance(line1: Line, line2: Line): Double {
        val theta = abs(line1.theta() -line2.theta()) % PI
        return if(0 <= line2.theta() && line2.theta() < PI/2){
            line1.length() * sin(theta)
        } else {
            line1.length()
        }
    }

    companion object {
        fun perpendicularDistance(line1: Line, line2: Line): Double {
            val l1 = line2.perpendicularDistance(line1.s)
            val l2 = line2.perpendicularDistance(line1.e)
            return (l1.pow(2)+l2.pow(2))/(l1 + l2)
        }

        fun parallelDistance(line1: Line, line2: Line): Double {
            val l1 = line2.parallelDistance(line1.s)
            val l2 = line2.parallelDistance(line1.e)

            return min(l1, l2)
        }

        fun angleDistance(line1: Line, line2: Line): Double {
            val theta = abs(line1.theta() -line2.theta()) % PI
            return if(0 <= line2.theta() && line2.theta() < PI/2){
                line1.length() * sin(theta)
            } else {
                line1.length()
            }
        }
    }

    fun theta(): Double = atan2(e.y-s.y, e.x-s.x)
}