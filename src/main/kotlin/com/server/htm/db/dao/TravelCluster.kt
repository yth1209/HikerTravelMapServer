package com.server.htm.db.dao

import com.server.htm.common.uuid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.locationtech.jts.geom.LineString

@Entity
@Table(name = "travel_cluster")
data class TravelCluster(
    @Id
    @Column(name = "path", columnDefinition = "geometry(LineString, 4326)")
    val path: LineString,

    @Column(name = "convenience_level")
    val convenienceLevel: String = "0"
)