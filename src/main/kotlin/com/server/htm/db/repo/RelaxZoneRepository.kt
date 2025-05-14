package com.server.htm.db.repo

import com.server.htm.db.dao.RelaxZone
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RelaxZoneRepository : JpaRepository<RelaxZone, String>