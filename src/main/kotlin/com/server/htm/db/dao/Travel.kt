package com.server.htm.db.dao

import com.server.htm.common.enum.RecordStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "travel")
data class Travel(
    @Id
    @Column(name = "id", columnDefinition = "char(32)")
    val id: String,

    @Column(name = "start_time")
    val startTime: OffsetDateTime,

    @Column(name = "end_time")
    var endTime: OffsetDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: RecordStatus
)