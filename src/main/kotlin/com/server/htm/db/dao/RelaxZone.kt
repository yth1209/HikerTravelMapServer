package com.server.htm.db.dao

import com.server.htm.common.uuid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.locationtech.jts.geom.Polygon

@Entity
@Table(name = "relax_zone")
data class RelaxZone(
    @Id
    @Column(name = "id")
    val id: String = uuid(),

    @Column(name = "area", columnDefinition = "geometry((polygon, 4326)")
    var area: Polygon,

    @Column(name = "type")
    val type: String = "0",

    @Column(name = "cnt")
    var cnt: Int
)