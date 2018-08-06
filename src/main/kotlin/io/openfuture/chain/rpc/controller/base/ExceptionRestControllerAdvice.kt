package io.openfuture.chain.rpc.controller.base

import io.openfuture.chain.rpc.domain.ExceptionResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@ControllerAdvice
@RestController
class ExceptionRestControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleException(ex: Exception): ExceptionResponse {
        return ExceptionResponse(HttpStatus.BAD_REQUEST.value(), ex.message)
    }

}
