package com.server.htm.db.repo

import com.server.htm.db.dao.TravelCluster
import org.locationtech.jts.geom.LineString
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TravelClusterRepository : JpaRepository<TravelCluster, LineString> {
}