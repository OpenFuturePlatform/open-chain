package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.TransferTransactionResponse
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@RestController
@RequestMapping("/rpc/transactions/transfer")
class TransferTransactionController(
    private val transactionService: TransferTransactionService
) {

    @GetMapping("/address/{address}")
    fun getTransactions(@PathVariable address: String): List<TransferTransactionResponse> =
        transactionService.getByAddress(address).map { TransferTransactionResponse(it) }

    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): TransferTransactionResponse = TransferTransactionResponse(transactionService.getByHash(hash))

    @PostMapping
    fun add(@Valid @RequestBody request: TransferTransactionRequest): TransferTransactionResponse {
        val tx = transactionService.add(request)
        return TransferTransactionResponse(tx)
    }

    @GetMapping
    fun getAll(request: PageRequest): PageResponse<TransferTransaction> = PageResponse(transactionService.getAll(request))

}

