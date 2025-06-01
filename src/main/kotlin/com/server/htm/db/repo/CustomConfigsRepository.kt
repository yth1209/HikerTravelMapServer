package com.server.htm.db.repo

import com.server.htm.db.dao.ConfigTypes
import com.server.htm.db.dao.CustomConfigs
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomConfigsRepository : JpaRepository<CustomConfigs, ConfigTypes> {

    fun findTopByType(type: ConfigTypes): CustomConfigs?
}