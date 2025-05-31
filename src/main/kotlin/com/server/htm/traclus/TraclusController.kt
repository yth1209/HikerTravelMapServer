package com.server.htm.traclus

import com.server.htm.common.dto.Response
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("traclus")
class TraclusController(
    private val traclusService: TraclusService
) {


    @PostMapping("partition/all")
    fun partitionAll(
    ): Response {
        return traclusService.partitionAll()
    }
}