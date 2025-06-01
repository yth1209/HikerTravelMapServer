package com.server.htm.view

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("view")
class ViewController(
    private val service: ViewService
) {

    @GetMapping("path/raw")
    fun getRawPath(
    ): GetGeomRes{
        return service.getRawPath()
    }

    @GetMapping("path/filtered")
    fun getFilterPath(
    ): GetGeomRes{
        return service.getFilteredPath()
    }

    @GetMapping("relax-zones")
    fun getRelaxZones(
    ): GetGeomRes{
        return service.getRelaxZones()
    }

}
