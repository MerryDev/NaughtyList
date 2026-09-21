package net.neruxvace.naughtylist.backend.web

sealed interface Patchable<out T> {
    data object Absent : Patchable<Nothing>
    data class Present<T>(val value: T?) : Patchable<T>
}

inline fun <T> Patchable<T>.ifPresent(action: (T?) -> Unit) {
    if (this is Patchable.Present) action(value)
}