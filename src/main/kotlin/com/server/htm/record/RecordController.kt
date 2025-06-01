package com.server.htm.record

import com.server.htm.record.dto.PostDataReq
import com.server.htm.record.dto.StartRecordReq
import com.server.htm.common.dto.Response
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("record")
class RecordController(
    private val recordService: RecordService
) {


    @PostMapping("start")
    fun startRecord(
        @RequestBody req: StartRecordReq
    ): Response {
        return recordService.startRecord(req)
    }

    @PostMapping("data")
    fun postData(
        @RequestBody req: PostDataReq
    ): Response {
        return recordService.postData(req)
    }

    @PutMapping("{travelId}/end")
    fun endRecord(
        @PathVariable("travelId") travelId: String
    ): Response{
        return recordService.endRecord(travelId)
    }
}