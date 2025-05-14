package com.server.htm.db.repo

import com.server.htm.db.dao.TravelData
import com.server.htm.db.dao.id.TravelDataId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TravelDataRepository : JpaRepository<TravelData, TravelDataId> {

    fun findAllByTravelIdOrderByTimestamp(travelId: String): List<TravelData>
}