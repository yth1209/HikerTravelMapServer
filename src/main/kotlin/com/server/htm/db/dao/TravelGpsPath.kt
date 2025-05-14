package com.server.htm.db.dao

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.locationtech.jts.geom.LineString

@Entity
@Table(name = "travel_gps_path")
data class TravelGpsPath(
    @Id
    @Column(name = "travel_id")
    val travelId: String,

    @Column(name = "raw_path", columnDefinition = "geometry(LineString, 4326)")
    val rawPath: LineString,

    @Column(name = "filtered_path", columnDefinition = "geometry(LineString, 4326)")
    var filteredPath: LineString? = null
)