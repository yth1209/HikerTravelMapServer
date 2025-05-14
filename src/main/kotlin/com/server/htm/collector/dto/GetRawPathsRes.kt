package com.server.htm.collector.dto

import com.server.htm.common.dto.Response
import org.locationtech.jts.geom.LineString

class GetRawPathsRes(
    val paths: List<List<List<Double>>>
): Response() {
}