package com.server.htm.traclus

import com.server.htm.common.dto.Response
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("traclus")
class TraclusController(
    private val traclusService: TraclusService
) {

    @PostMapping("all/filter")
    fun filterAllRecord(
    ): Response {
        return traclusService.filterAllRecord()
    }

    @PostMapping("partition/all")
    fun partitionAll(
    ): Response {
        return traclusService.partitionAll()
    }

    @PostMapping("global/relaxzone/all")
    fun createGlobalRelaxZoneAll(
    ): Response {
        return traclusService.createGlobalRelaxZoneAll()
    }

    @PostMapping("cluster/all")
    fun mergeAllGlobalCluster(
    ): Response {
        return traclusService.mergeAllGlobalCluster()
    }

    @PostMapping("travel/{travelId}/cluster/all")
    fun merge2GlobalCluster(
        @PathVariable("travelId") travelId: String,
    ): Response {
        return traclusService.merge2GlobalCluster(travelId)
    }

}