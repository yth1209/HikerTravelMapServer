package com.server.htm.db.repo

import com.server.htm.db.dao.TravelCluster
import org.locationtech.jts.geom.LineString
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TravelClusterRepository : JpaRepository<TravelCluster, LineString> {

    @Query("""
        SELECT DISTINCT tc.*
        FROM travel_cluster tc
        WHERE EXISTS (
            SELECT 1
            FROM travel_segment ts
            WHERE ts.travel_id = :travelId
              AND ST_DWithin(tc.path::geography, ts.path::geography, 10)
        )
        """,
        nativeQuery = true
    )
    fun findAllNearClusterByTravelId(travelId: String): List<TravelCluster>

}