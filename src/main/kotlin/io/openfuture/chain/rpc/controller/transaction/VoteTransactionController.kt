package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.rpc.domain.transaction.request.VoteTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.VoteTransactionResponse
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions/vote")
class VoteTransactionController(
    private val transactionManager: TransactionManager
) {

    @CrossOrigin
    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): VoteTransactionResponse =
        VoteTransactionResponse(transactionManager.getVoteTransactionByHash(hash))

    @PostMapping
    fun add(@Valid @RequestBody request: VoteTransactionRequest): VoteTransactionResponse {
        val tx = transactionManager.add(UnconfirmedVoteTransaction.of(request))
        return VoteTransactionResponse(tx)
    }

}