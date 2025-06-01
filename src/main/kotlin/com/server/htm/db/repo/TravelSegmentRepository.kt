package com.server.htm.db.repo

import com.server.htm.db.dao.TravelSegment
import org.locationtech.jts.geom.LineString
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TravelSegmentRepository : JpaRepository<TravelSegment, String> {

    @Query("""
        SELECT ts.lineSegment FROM TravelSegment ts
    """)
    fun findAllLineSegment() : List<LineString>

    @Query("""
        SELECT ts.lineSegment FROM TravelSegment ts
        where ts.travelId = :travelId
    """)
    fun findAllLineSegmentByTravelId(travelId: String) : List<LineString>
}