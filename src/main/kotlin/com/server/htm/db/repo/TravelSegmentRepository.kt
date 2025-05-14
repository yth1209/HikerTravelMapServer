package com.server.htm.db.repo

import com.server.htm.db.dao.TravelSegment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TravelSegmentRepository : JpaRepository<TravelSegment, String>