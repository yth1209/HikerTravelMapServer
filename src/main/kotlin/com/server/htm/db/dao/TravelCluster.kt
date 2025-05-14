package com.server.htm.db.dao

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "travel_cluster")
data class TravelCluster(
    @Id
    @Column(name = "id")
    val id: String,

    @Column(name = "path", columnDefinition = "path")
    val path: String,

    @Column(name = "convenience_level")
    val convenienceLevel: String
)