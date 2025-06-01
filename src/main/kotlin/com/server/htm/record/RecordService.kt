package com.server.htm.record

import com.server.htm.record.dto.PostDataReq
import com.server.htm.record.dto.StartRecordReq
import com.server.htm.common.model.GPSFilter
import com.server.htm.common.dto.Response
import com.server.htm.common.enum.ActivityType
import com.server.htm.common.enum.RecordStatus
import com.server.htm.db.dao.Travel
import com.server.htm.db.dao.TravelData
import com.server.htm.db.dao.TravelGpsPath
import com.server.htm.db.repo.TravelDataRepository
import com.server.htm.db.repo.TravelGpsPathRepository
import com.server.htm.db.repo.TravelRelaxZoneRepository
import com.server.htm.db.repo.TravelRepository
import com.server.htm.traclus.TraclusService
import jakarta.transaction.Transactional
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class RecordService(
    private val travelRepo: TravelRepository,
    private val travelDataRepo: TravelDataRepository,
    private val travelGpsPathRepo: TravelGpsPathRepository,
    private val travelRelaxZoneRepo: TravelRelaxZoneRepository,
    private val traclusService: TraclusService
    ) {
    private val geometryFactory = GeometryFactory()

    fun startRecord(req: StartRecordReq): Response {
        if(travelRepo.existsById(req.travelId))
            return Response("Travel Already Exists")

        travelRepo.save(
            Travel(
                id = req.travelId,
                startTime = OffsetDateTime.now(),
                status = RecordStatus.RECORDING
            )
        )

        return Response()
    }

    fun postData(
        req: PostDataReq
    ): Response {
        val travel = travelRepo.findByIdOrNull(req.travelId) ?: return Response("No such travel")

        travelDataRepo.save(
            TravelData(
                travelId = travel.id,
                gps = geometryFactory.createPoint(Coordinate(req.lng, req.lat)),
                accuracy = req.accuracy,
                activity = ActivityType.valueOf(req.activityType)
            )
        )

        return Response()
    }

    @Transactional
    fun endRecord(travelId: String): Response {
        val travel = travelRepo.findByIdOrNull(travelId) ?: return Response("No such travel")
        travel.endTime = OffsetDateTime.now()
        travel.status = RecordStatus.DONE

        travelRepo.save(travel)

        val datas = travelDataRepo.findAllByTravelIdOrderByTimestamp(travel.id)
        val rawPath = geometryFactory.createLineString(datas.mapNotNull { it.gps.coordinate }.toTypedArray())

        val gpsFilter = GPSFilter(datas)
        val filteredPath = gpsFilter.filteredLineStr
        val travelGpsPath = travelGpsPathRepo.save(
            TravelGpsPath(
                travelId = travel.id,
                rawPath = rawPath,
                filteredPath = filteredPath
            )
        )
        traclusService.partition(travelGpsPath)

        traclusService.saveTravelRelaxZone(gpsFilter, travel.id)
        traclusService.mergeToGlobalRelaxZone(travel)

        return Response()
    }

}