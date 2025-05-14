package com.server.htm.db.dao.id

import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class ClusterMappingId(
    val segmentId: String,
    val globalSegmentId: String
) : Serializable