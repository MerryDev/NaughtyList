package net.neruxvace.naughtylist.backend.persistence

fun <T : Any> T?.required(): T = requireNotNull(this) { "Expected non-null database value" }