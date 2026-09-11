package net.neruxvace.naughtylist.backend.reason.request

data class UpdateReasonRequest(
    val name: String? = null,
    val description: String? = null,
    val enabled: Boolean? = null
) {

    fun isEmpty() = name == null && description == null && enabled == null

}
