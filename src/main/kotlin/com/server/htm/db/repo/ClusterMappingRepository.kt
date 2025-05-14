package com.server.htm.db.repo

import com.server.htm.db.dao.ClusterMapping
import com.server.htm.db.dao.id.ClusterMappingId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClusterMappingRepository : JpaRepository<ClusterMapping, ClusterMappingId>