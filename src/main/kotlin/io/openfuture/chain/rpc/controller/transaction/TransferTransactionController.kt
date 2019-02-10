package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.crypto.annotation.AddressChecksum
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.transaction.request.TransactionPageRequest
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.TransferTransactionResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@RestController
@Validated
@RequestMapping("/rpc/transactions/transfer")
class TransferTransactionController(
    private val transactionService: TransferTransactionService
) {

    @CrossOrigin
    @GetMapping("/address/{address}")
    fun getTransactions(@PathVariable @AddressChecksum address: String, @Valid request: TransactionPageRequest): PageResponse<TransferTransactionResponse> =
        PageResponse(transactionService.getByAddress(address, request).map { TransferTransactionResponse(it) })

    @CrossOrigin
    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): TransferTransactionResponse =
        TransferTransactionResponse(transactionService.getByHash(hash))

    @PostMapping
    fun add(@Valid @RequestBody request: TransferTransactionRequest): TransferTransactionResponse =
        TransferTransactionResponse(transactionService.add(UnconfirmedTransferTransaction.of(request)))

    @CrossOrigin
    @GetMapping
    fun getAll(@Valid request: TransactionPageRequest): PageResponse<TransferTransactionResponse> =
        PageResponse(transactionService.getAll(request).map { TransferTransactionResponse(it) })

}

