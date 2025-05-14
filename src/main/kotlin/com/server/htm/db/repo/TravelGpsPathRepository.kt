package com.server.htm.db.repo

import com.server.htm.db.dao.TravelGpsPath
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TravelGpsPathRepository : JpaRepository<TravelGpsPath, String>{
    fun findTopByTravelId(travelId: String): TravelGpsPath?
}