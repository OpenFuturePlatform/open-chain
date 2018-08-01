package io.openfuture.chain.rpc.controller

import io.openfuture.chain.network.domain.application.transaction.DelegateTransactionMessage
import io.openfuture.chain.network.domain.application.transaction.TransferTransactionMessage
import io.openfuture.chain.network.domain.application.transaction.VoteTransactionMessage
import io.openfuture.chain.network.domain.application.transaction.data.DelegateTransactionData
import io.openfuture.chain.network.domain.application.transaction.data.TransferTransactionData
import io.openfuture.chain.network.domain.application.transaction.data.VoteTransactionData
import io.openfuture.chain.rpc.controller.base.BaseController
import io.openfuture.chain.rpc.domain.RestResponse
import io.openfuture.chain.rpc.domain.transaction.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.TransferTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.VoteTransactionRequest
import io.openfuture.chain.service.UDelegateTransactionService
import io.openfuture.chain.service.UTransferTransactionService
import io.openfuture.chain.service.UVoteTransactionService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions")
class TransactionController(
    private val uVoteTransactionService: UVoteTransactionService,
    private val uTransferTransactionService: UTransferTransactionService,
    private val uDelegateTransactionService: UDelegateTransactionService
) : BaseController() {

    @PostMapping("/votes/doGenerateHash")
    fun getVoteBytes(@Valid @RequestBody data: VoteTransactionData): RestResponse<ByteArray> {
        return RestResponse(getResponseHeader(), data.getBytes())
    }

    @PostMapping("/votes")
    fun addVote(@Valid @RequestBody request: VoteTransactionRequest): RestResponse<VoteTransactionMessage> {
        val tx = uVoteTransactionService.add(request)
        return RestResponse(getResponseHeader(), VoteTransactionMessage(tx))
    }

    @PostMapping("/transfers/doGenerateHash")
    fun getTransferBytes(@Valid @RequestBody data: TransferTransactionData): RestResponse<ByteArray> {
        return RestResponse(getResponseHeader(), data.getBytes())
    }

    @PostMapping("/transfers")
    fun addTransfer(@Valid @RequestBody request: TransferTransactionRequest): RestResponse<TransferTransactionMessage> {
        val tx = uTransferTransactionService.add(request)
        return RestResponse(getResponseHeader(), TransferTransactionMessage(tx))
    }

    @PostMapping("/delegates/doGenerateHash")
    fun getDelegateBytes(@Valid @RequestBody data: DelegateTransactionData): RestResponse<ByteArray> {
        return RestResponse(getResponseHeader(), data.getBytes())
    }

    @PostMapping("/delegates")
    fun addDelegates(@Valid @RequestBody request: DelegateTransactionRequest): RestResponse<DelegateTransactionMessage> {
        val tx = uDelegateTransactionService.add(request)
        return RestResponse(getResponseHeader(), DelegateTransactionMessage(tx))
    }

}
