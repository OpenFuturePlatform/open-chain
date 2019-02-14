package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.service.ReceiptService
import io.openfuture.chain.core.service.TransactionManager
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
    private val transactionManager: TransactionManager,
    private val receiptService: ReceiptService
) {

    @CrossOrigin
    @GetMapping("/address/{address}")
    fun getTransactions(@PathVariable @AddressChecksum address: String, @Valid request: TransactionPageRequest): PageResponse<TransferTransactionResponse> =
        PageResponse(transactionManager.getAllTransferTransactionsByAddress(address, request).map {
            TransferTransactionResponse(it, receiptService.getByTransactionHash(it.hash))
        })

    @CrossOrigin
    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): TransferTransactionResponse {
        val tx = transactionManager.getTransferTransactionByHash(hash)
        val receipt = receiptService.getByTransactionHash(hash)
        return TransferTransactionResponse(tx, receipt)
    }

    @PostMapping
    fun add(@Valid @RequestBody request: TransferTransactionRequest): TransferTransactionResponse =
        TransferTransactionResponse(transactionManager.add(UnconfirmedTransferTransaction.of(request)))

    @CrossOrigin
    @GetMapping
    fun getAll(@Valid request: TransactionPageRequest): PageResponse<TransferTransactionResponse> =
        PageResponse(transactionManager.getAllTransferTransactions(request).map {
            TransferTransactionResponse(it, receiptService.getByTransactionHash(it.hash))
        })

}

