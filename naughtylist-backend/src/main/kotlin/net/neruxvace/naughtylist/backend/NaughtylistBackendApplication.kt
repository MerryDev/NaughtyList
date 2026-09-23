package net.neruxvace.naughtylist.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NaughtylistBackendApplication

fun main(args: Array<String>) {
    runApplication<NaughtylistBackendApplication>(*args)
}
