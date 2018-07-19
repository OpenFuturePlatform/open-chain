package io.openfuture.chain.controller

import io.openfuture.chain.domain.rpc.transaction.DelegateTransactionRequest
import io.openfuture.chain.domain.rpc.transaction.TransferTransactionRequest
import io.openfuture.chain.domain.rpc.transaction.VoteTransactionRequest
import io.openfuture.chain.domain.transaction.DelegateTransactionDto
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.service.DelegateTransactionService
import io.openfuture.chain.service.TransferTransactionService
import io.openfuture.chain.service.VoteTransactionService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("${PathConstant.RPC}/transactions")
class TransactionController(
    private val voteTransactionService: VoteTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val delegateTransactionService: DelegateTransactionService
) {

    @PostMapping("/votes")
    fun addVote(@Valid @RequestBody request: VoteTransactionRequest): VoteTransactionDto {
        val tx = voteTransactionService.add(request)
        return VoteTransactionDto(tx)
    }

    @PostMapping("/transfers")
    fun addTransfer(@Valid @RequestBody request: TransferTransactionRequest): TransferTransactionDto {
        val tx = transferTransactionService.add(request)
        return TransferTransactionDto(tx)
    }

    @PostMapping("/delegates")
    fun addDelegates(@Valid @RequestBody request: DelegateTransactionRequest): DelegateTransactionDto {
        val tx = delegateTransactionService.add(request)
        return DelegateTransactionDto(tx)
    }

}