package com.server.htm.db.dao

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "relax_zone")
data class RelaxZone(
    @Id
    @Column(name = "id")
    val id: String,

    @Column(name = "area", columnDefinition = "polygon")
    val area: String,

    @Column(name = "type")
    val type: String,

    @Column(name = "cnt")
    val cnt: Int
)