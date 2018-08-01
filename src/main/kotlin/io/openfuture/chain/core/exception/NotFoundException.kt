package io.openfuture.chain.core.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Not Found")
open class NotFoundException(message: String?) : RuntimeException(message)