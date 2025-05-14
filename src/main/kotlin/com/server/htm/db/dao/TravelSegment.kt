package com.server.htm.db.dao

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "travel_segment")
data class TravelSegment(
    @Id
    @Column(name = "segment_id")
    val segmentId: String,

    @Column(name = "travel_id")
    val travelId: String,

    @Column(name = "path", columnDefinition = "path")
    val path: String,

    @Column(name = "convenience_level")
    val convenienceLevel: String,

    @Column(name = "line_segment", columnDefinition = "lseg")
    val lineSegment: String
)