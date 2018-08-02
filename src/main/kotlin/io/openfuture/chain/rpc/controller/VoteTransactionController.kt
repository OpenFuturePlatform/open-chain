package io.openfuture.chain.rpc.controller

import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.rpc.domain.transaction.request.VoteTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.VoteTransactionResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions/votes")
class TransferTransactionController(
    private val voteService: VoteTransactionService
) {

    @PostMapping("/doGenerateHash")
    fun getBytes(@Valid @RequestBody payload: VoteTransactionPayload): ByteArray = voteService.getBytes(payload)

    @PostMapping
    fun add(@Valid @RequestBody request: VoteTransactionRequest): VoteTransactionResponse {
        val tx = voteService.add(request)
        return VoteTransactionResponse(tx)
    }

}