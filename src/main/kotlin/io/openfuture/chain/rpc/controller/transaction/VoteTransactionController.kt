package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.rpc.domain.transaction.request.VoteTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.VoteTransactionResponse
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions/votes")
class VoteTransactionController(
    private val transactionService: VoteTransactionService
) {

    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): VoteTransaction = transactionService.getByHash(hash)

    @PostMapping
    fun add(@Valid @RequestBody request: VoteTransactionRequest): VoteTransactionResponse {
        val tx = transactionService.add(request)
        return VoteTransactionResponse(tx)
    }

}