package net.neruxvace.naughtylist.backend.report

import jakarta.validation.Valid
import net.neruxvace.naughtylist.backend.auth.CurrentClient
import net.neruxvace.naughtylist.backend.report.request.CreateReportRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/reports")
class ReportController(
    private val service: ReportService,
    private val currentClient: CurrentClient
) {

    @GetMapping
    fun getReports(): List<ReportResponse> {
        return service.findAll()
    }

    @GetMapping("/{id}")
    fun getReport(@PathVariable id: Long): ResponseEntity<ReportResponse> {
        val report = service.findById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(report)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createReport(@Valid @RequestBody request: CreateReportRequest): ReportResponse {
        val client = currentClient.requireServer()
        return service.create(request, client.clientId)
    }

}