package net.neruxvace.naughtylist.backend.auth

object PermissionMapping {

    private val mapping = mapOf(
        "naughtylist.reason.write" to "reason:write",
        "naughtylist.report.review" to "report:review",
        "naughtylist.case.write" to "case:write"
    )

    fun scopeFor(permissions: Set<String>): Set<String> {
        return permissions.mapNotNull(mapping::get).toSet()
    }
}