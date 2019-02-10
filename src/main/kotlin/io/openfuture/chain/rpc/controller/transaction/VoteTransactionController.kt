package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.rpc.domain.transaction.request.VoteTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.VoteTransactionResponse
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions/vote")
class VoteTransactionController(
    private val transactionService: VoteTransactionService
) {

    @CrossOrigin
    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): VoteTransactionResponse = VoteTransactionResponse(transactionService.getByHash(hash))

    @PostMapping
    fun add(@Valid @RequestBody request: VoteTransactionRequest): VoteTransactionResponse {
        val tx = transactionService.add(UnconfirmedVoteTransaction.of(request))
        return VoteTransactionResponse(tx)
    }

}