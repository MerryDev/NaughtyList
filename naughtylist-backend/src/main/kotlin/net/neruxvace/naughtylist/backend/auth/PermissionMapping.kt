package net.neruxvace.naughtylist.backend.auth

object PermissionMapping {

    private val mapping = mapOf("naughtylist.test.test" to "test:test")

    fun scopeFor(permissions: Set<String>): Set<String> {
        return permissions.mapNotNull(mapping::get).toSet()
    }
}