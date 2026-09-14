package net.neruxvace.naughtylist.backend.report.request

import jakarta.validation.constraints.Positive

data class UpdateReportCaseRequest(@Positive val caseId: Long?)
