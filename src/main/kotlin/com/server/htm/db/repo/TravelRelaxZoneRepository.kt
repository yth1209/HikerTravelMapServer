package com.server.htm.db.repo

import com.server.htm.db.dao.RelaxZone
import com.server.htm.db.dao.TravelRelaxZone
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TravelRelaxZoneRepository : JpaRepository<TravelRelaxZone, String> {
    fun deleteAllByTravelId(travelId: String)

    fun findAllByTravelIdAndAreaIsNotNull(travelId: String): List<TravelRelaxZone>
}