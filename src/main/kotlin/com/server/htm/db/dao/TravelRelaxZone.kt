package com.server.htm.db.dao

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.locationtech.jts.geom.MultiPoint
import org.locationtech.jts.geom.Point
import java.util.UUID

@Entity
@Table(name = "travel_relax_zone")
data class TravelRelaxZone(
    @Id
    @Column(name = "id", columnDefinition = "char(32)")
    val id: String = UUID.randomUUID().toString().replace("-", ""),

    @Column(name = "travel_id")
    val travelId: String,

    @Column(name = "area", columnDefinition = "geometry((multipoint, 4326)")
    val area: MultiPoint,

    @Column(name = "type")
    val type: String,

    @Column(name = "cnt")
    val cnt: Int
)