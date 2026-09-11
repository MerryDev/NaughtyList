package net.neruxvace.naughtylist.backend.report

import jakarta.validation.Valid
import net.neruxvace.naughtylist.backend.auth.CurrentClient
import net.neruxvace.naughtylist.backend.report.request.CreateReportRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reports")
class ReportController(
    private val service: ReportService,
    private val currentClient: CurrentClient
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createReport(@Valid @RequestBody request: CreateReportRequest): ReportResponse {
        val client = currentClient.requireServer()
        return service.create(request, client.clientId)
    }

}