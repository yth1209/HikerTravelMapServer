package com.server.htm.db.dao

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.locationtech.jts.geom.LineString
import java.util.UUID

@Entity
@Table(name = "travel_segment")
data class TravelSegment(
    @Id
    @Column(name = "segment_id")
    val segmentId: String = UUID.randomUUID().toString().replace("-", ""),

    @Column(name = "travel_id")
    val travelId: String,

    @Column(name = "path", columnDefinition = "geometry(LineString, 4326)")
    val path: LineString,

    @Column(name = "convenience_level")
    val convenienceLevel: String,

    @Column(name = "line_segment", columnDefinition = "geometry(LineString, 4326)")
    val lineSegment: LineString,

    @Column(name = "idx")
    val idx: Int
)