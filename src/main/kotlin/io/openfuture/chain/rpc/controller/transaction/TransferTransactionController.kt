package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.rpc.domain.transaction.request.transfer.TransferTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.transfer.TransferTransactionRequest
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
    fun getHash(@Valid @RequestBody request: TransferTransactionHashRequest): String {
        return transactionService.generateHash(request)
    }

    @PostMapping
    fun add(@Valid @RequestBody request: TransferTransactionRequest): TransferTransactionResponse {
        val tx = transactionService.add(request)
        return TransferTransactionResponse(tx)
    }

}

