package com.server.htm.db.dao

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "custom_configs")
data class CustomConfigs(
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    val type: ConfigTypes,

    @Column(columnDefinition = "json")
    val configs: String
) {

    fun getJsonValue(key: String): Double? {
        return try {
            val mapper = jacksonObjectMapper()
            val map: Map<String, Any?> = mapper.readValue(configs)
            map[key]?.toString()?.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }
}

enum class ConfigTypes(){
    TRAVEL_SEGMENTATION,
    RELAX_ZONE
}