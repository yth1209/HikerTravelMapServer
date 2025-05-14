package com.server.htm.db.dao

// Kotlin + Spring Boot JPA Entities based on the PostgreSQL schema

import com.server.htm.db.dao.id.ClusterMappingId
import jakarta.persistence.*

@Entity
@Table(name = "cluster_mapping")
@IdClass(ClusterMappingId::class)
data class ClusterMapping(
    @Id
    @Column(name = "segment_id")
    val segmentId: String,

    @Id
    @Column(name = "global_segment_id")
    val globalSegmentId: String
)