package net.neruxvace.naughtylist.backend.reason

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reasons")
class ReasonController(private val service: ReasonService) {

    @GetMapping
    fun getReasons(): List<ReasonResponse> {
        return service.findAll()
    }

    @GetMapping("/{id}")
    fun getReason(@PathVariable id: Long): ResponseEntity<ReasonResponse> {
        val reason = service.findById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(reason)
    }

}