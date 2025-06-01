package com.server.htm.db.repo

import com.server.htm.db.dao.RelaxZone
import org.locationtech.jts.geom.Polygon
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RelaxZoneRepository : JpaRepository<RelaxZone, String> {

    @Query("""
        SELECT * FROM relax_zone rz 
    WHERE ST_DWithin(
        ST_Transform(rz.area::geometry, 3857),             
        ST_Transform(CAST(:polygon AS geometry), 3857),
        :distance
    )
    ORDER BY ST_Distance(
        ST_Transform(rz.area::geometry, 3857),
        ST_Transform(CAST(:polygon AS geometry), 3857)
    )
    LIMIT 1;
    """,
        nativeQuery = true)
    fun findTopNearbyPolygon(@Param("polygon") polygon: Polygon, @Param("distance") distance: Double): RelaxZone?
}