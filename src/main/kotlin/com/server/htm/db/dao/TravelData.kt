package com.server.htm.db.dao

import com.server.htm.common.enum.ActivityType
import com.server.htm.db.dao.id.TravelDataId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Point
import java.time.OffsetDateTime

@Entity
@Table(name = "travel_data")
@IdClass(TravelDataId::class)
data class TravelData(
    @Id
    @Column(name = "timestamp")
    var timestamp: OffsetDateTime = OffsetDateTime.now(),

    @Id
    @Column(name = "travel_id", columnDefinition = "char(32)")
    val travelId: String,

    @Column(name = "gps", columnDefinition = "geometry(Point, 4326)")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    val gps: Point, // Consider using custom Point type

    @Column(name = "accuracy")
    val accuracy: Double = -1.0,

    @Enumerated(EnumType.STRING)
    @Column(name = "activity")
    val activity: ActivityType,

    @Column(name = "is_valid")
    val isValid: Boolean = true
)