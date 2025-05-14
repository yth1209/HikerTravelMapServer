package com.server.htm.db.repo

import com.server.htm.db.dao.TravelCluster
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TravelClusterRepository : JpaRepository<TravelCluster, String>