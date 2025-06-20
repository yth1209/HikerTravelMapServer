package com.server.htm.db.repo

import com.server.htm.common.enum.RecordStatus
import com.server.htm.db.dao.Travel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TravelRepository : JpaRepository<Travel, String> {

    fun findAllByStatus(status: RecordStatus): List<Travel>
}