package io.openfuture.chain.rpc.controller

import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.rpc.domain.transaction.request.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.DelegateTransactionResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions/delegates")
class TransferTransactionController(
    private val transactionService: DelegateTransactionService
) {

    @PostMapping("/doGenerateHash")
    fun getBytes(@Valid @RequestBody payload: DelegateTransactionPayload): ByteArray = transactionService.getBytes(payload)

    @PostMapping
    fun add(@Valid @RequestBody request: DelegateTransactionRequest): DelegateTransactionResponse {
        val tx = transactionService.add(request)
        return DelegateTransactionResponse(tx)
    }

}

