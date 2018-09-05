package io.openfuture.chain.rpc.controller.base

import io.openfuture.chain.core.exception.SynchronizationException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.rpc.domain.ExceptionResponse
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@ControllerAdvice
@RestController
class ExceptionRestControllerAdvice {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleException(ex: Exception): ExceptionResponse {
        return ExceptionResponse(BAD_REQUEST.value(), ex.message)
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ExceptionResponse {
        return ExceptionResponse(BAD_REQUEST.value(), ex.message, ex.type?.name)
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(SynchronizationException::class)
    fun handleSynchronizationException(ex: SynchronizationException): ExceptionResponse {
        return ExceptionResponse(BAD_REQUEST.value(), "Blockchain is synchronizing")
    }

}
