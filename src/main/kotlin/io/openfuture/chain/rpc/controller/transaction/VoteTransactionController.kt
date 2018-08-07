package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.rpc.domain.transaction.request.vote.VoteTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.vote.VoteTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.VoteTransactionResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions/votes")
class VoteTransactionController(
    private val voteService: VoteTransactionService
) {

    @PostMapping("/doGenerateHash")
    fun getHash(@Valid @RequestBody request: VoteTransactionHashRequest): String {
        return voteService.generateHash(request)
    }

    @PostMapping
    fun add(@Valid @RequestBody request: VoteTransactionRequest): VoteTransactionResponse {
        val tx = voteService.add(request)
        return VoteTransactionResponse(tx)
    }

}