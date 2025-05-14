package com.server.htm.collector.dto

class PostDataReq(
    val travelId: String,
    val lat: Double,
    val lng: Double,
    val accuracy: Double,
    val activityType: String
) {
}