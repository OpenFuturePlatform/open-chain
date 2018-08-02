package io.openfuture.chain.rpc.controller

import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.TransferTransactionResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid


@RestController
@RequestMapping("/rpc/transactions/transfer")
class TransferTransactionController(
    private val transactionService: TransferTransactionService
) {

    @PostMapping("/doGenerateHash")
    fun getBytes(@Valid @RequestBody payload: TransferTransactionPayload): ByteArray = transactionService.getBytes(payload)

    @PostMapping
    fun add(@Valid @RequestBody request: TransferTransactionRequest): TransferTransactionResponse {
        val tx = transactionService.add(request)
        return TransferTransactionResponse(tx)
    }

}

