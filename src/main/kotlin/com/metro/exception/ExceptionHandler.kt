package com.metro.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(value = [IllegalArgumentException::class])
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun handleIllegalArgument(exception: RuntimeException, request: WebRequest): ErrorResponse {
        return ErrorResponse(exception.message!!)
    }

    @ExceptionHandler(value = [MetroClientException::class])
    @ResponseStatus(code = HttpStatus.FAILED_DEPENDENCY)
    @ResponseBody
    fun handleMetroClientException(exception: MetroClientException, request: WebRequest): ErrorResponse {
        return ErrorResponse(exception.message)
    }

    data class ErrorResponse(val message: String)

    data class MetroClientException(override val message: String) : Exception()
}