package com.server.htm.db.dao

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "travel_relax_zone")
data class TravelRelaxZone(
    @Id
    @Column(name = "id", columnDefinition = "char(32)")
    val id: String,

    @Column(name = "travel_id")
    val travelId: String,

    @Column(name = "area", columnDefinition = "polygon")
    val area: String?,

    @Column(name = "type")
    val type: String,

    @Column(name = "cnt")
    val cnt: Int
)