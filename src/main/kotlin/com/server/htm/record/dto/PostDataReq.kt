package com.server.htm.record.dto

class PostDataReq(
    val travelId: String,
    val lat: Double,
    val lng: Double,
    val accuracy: Double,
    val activityType: String
) {
}