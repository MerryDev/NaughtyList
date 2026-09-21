package net.neruxvace.naughtylist.backend.report

import jakarta.validation.Valid
import net.neruxvace.naughtylist.backend.auth.CurrentActor
import net.neruxvace.naughtylist.backend.auth.CurrentClient
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.jooq.enums.ReportStatus
import net.neruxvace.naughtylist.backend.report.request.CreateReportRequest
import net.neruxvace.naughtylist.backend.report.request.UpdateReportCaseRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import kotlin.uuid.Uuid

@RestController
@RequestMapping("/api/v1/reports")
class ReportController(
    private val service: ReportService,
    private val currentClient: CurrentClient,
    private val currentActor: CurrentActor
) {

    @GetMapping
    fun getReports(
        @RequestParam(required = false) status: ReportStatus?,
        @RequestParam(required = false) targetUuid: Uuid?,
        @RequestParam(required = false) serverName: String?,
        @RequestParam(required = false) caseId: Long?
    ): List<ReportResponse> = service.findAll(status, targetUuid, serverName, caseId)


    @GetMapping("/{id}")
    fun getReport(@PathVariable id: Long): ReportResponse = service.findById(id)
        ?: throw ResourceNotFoundException("Report not found")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createReport(@Valid @RequestBody request: CreateReportRequest): ReportResponse {
        val client = currentClient.requireServer()

        return service.create(request, client.clientId)
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('SCOPE_report:review')")
    fun closeReport(@PathVariable id: Long): ReportResponse = service.close(id)
        ?: throw ResourceNotFoundException("Report not found")

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAuthority('SCOPE_report:review')")
    fun acceptReport(@PathVariable id: Long): ReportResponse {
        val actor = currentActor.requireActor()

        return service.accept(id, actor.playerUuid)
            ?: throw ResourceNotFoundException("Report not found")
    }

    @PutMapping("/{id}/case")
    @PreAuthorize("hasAuthority('SCOPE_report:review')")
    fun updateReportCase(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateReportCaseRequest
    ): ReportResponse = service.updateCase(id, request)
        ?: throw ResourceNotFoundException("Report not found")

}