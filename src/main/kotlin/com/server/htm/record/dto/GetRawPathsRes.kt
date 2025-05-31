package com.server.htm.record.dto

import com.server.htm.common.dto.Response

class GetRawPathsRes(
    val paths: List<List<List<Double>>>
): Response() {
}