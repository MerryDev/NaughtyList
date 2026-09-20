package net.neruxvace.naughtylist.backend.web

import net.neruxvace.naughtylist.backend.exception.*
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApplicationExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun notFound(exception: ResourceNotFoundException) = response(HttpStatus.NOT_FOUND, exception)

    @ExceptionHandler(ResourceConflictException::class)
    fun conflict(exception: ResourceConflictException) = response(HttpStatus.CONFLICT, exception)

    @ExceptionHandler(InvalidRequestException::class)
    fun invalidRequest(exception: InvalidRequestException) = response(HttpStatus.BAD_REQUEST, exception)

    @ExceptionHandler(InvalidCredentialsException::class)
    fun invalidCredentials(exception: InvalidCredentialsException) = response(HttpStatus.UNAUTHORIZED, exception)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validationFailed(exception: MethodArgumentNotValidException): ResponseEntity<ProblemDetail> {
        val errors = exception.bindingResult.fieldErrors.groupBy(
            keySelector = { it.field },
            valueTransform = { it.defaultMessage ?: "Invalid value" }
        )
        return response(HttpStatus.BAD_REQUEST, "Request validation failed") {
            setProperty("errors", errors)
        }
    }

    private fun response(status: HttpStatus, exception: ApplicationException): ResponseEntity<ProblemDetail> {
        val body = ProblemDetail.forStatusAndDetail(status, exception.message ?: status.reasonPhrase)

        return ResponseEntity(body, status)
    }

    private fun response(status: HttpStatus, detail: String, configure: ProblemDetail.() -> Unit = {}): ResponseEntity<ProblemDetail> {
        val body = ProblemDetail.forStatusAndDetail(status, detail).apply(configure)

        return ResponseEntity(body, status)
    }

}