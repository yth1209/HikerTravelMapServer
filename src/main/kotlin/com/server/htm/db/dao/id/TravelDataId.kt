package com.server.htm.db.dao.id

import jakarta.persistence.Embeddable
import java.io.Serializable
import java.time.OffsetDateTime
import java.time.ZonedDateTime

@Embeddable
data class TravelDataId(
    val timestamp: OffsetDateTime,
    val travelId: String
) : Serializable