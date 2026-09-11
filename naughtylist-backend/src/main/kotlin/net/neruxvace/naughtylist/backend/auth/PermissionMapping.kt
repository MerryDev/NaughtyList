package net.neruxvace.naughtylist.backend.auth

object PermissionMapping {

    private val mapping = mapOf("naughtylist.reason.write" to "reason:write")

    fun scopeFor(permissions: Set<String>): Set<String> {
        return permissions.mapNotNull(mapping::get).toSet()
    }
}