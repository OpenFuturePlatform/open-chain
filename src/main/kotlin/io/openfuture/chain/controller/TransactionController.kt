package io.openfuture.chain.controller

import io.openfuture.chain.controller.common.BaseController
import io.openfuture.chain.controller.common.RestResponse
import io.openfuture.chain.domain.rpc.transaction.DelegateTransactionRequest
import io.openfuture.chain.domain.rpc.transaction.TransferTransactionRequest
import io.openfuture.chain.domain.rpc.transaction.VoteTransactionRequest
import io.openfuture.chain.domain.transaction.DelegateTransactionDto
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.service.UDelegateTransactionService
import io.openfuture.chain.service.UTransferTransactionService
import io.openfuture.chain.service.UVoteTransactionService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("${PathConstant.RPC}/transactions")
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
    fun addVote(@Valid @RequestBody request: VoteTransactionRequest): RestResponse<VoteTransactionDto> {
        val tx = uVoteTransactionService.add(request)
        return RestResponse(getResponseHeader(), VoteTransactionDto(tx))
    }

    @PostMapping("/transfers/doGenerateHash")
    fun getTransferBytes(@Valid @RequestBody data: TransferTransactionData): RestResponse<ByteArray> {
        return RestResponse(getResponseHeader(), data.getBytes())
    }

    @PostMapping("/transfers")
    fun addTransfer(@Valid @RequestBody request:TransferTransactionRequest): RestResponse<TransferTransactionDto> {
        val tx = uTransferTransactionService.add(request)
        return RestResponse(getResponseHeader(), TransferTransactionDto(tx))
    }

    @PostMapping("/delegates/doGenerateHash")
    fun getDelegateBytes(@Valid @RequestBody data: DelegateTransactionData): RestResponse<ByteArray> {
        return RestResponse(getResponseHeader(), data.getBytes())
    }

    @PostMapping("/delegates")
    fun addDelegates(@Valid @RequestBody request: DelegateTransactionRequest): RestResponse<DelegateTransactionDto> {
        val tx = uDelegateTransactionService.add(request)
        return RestResponse(getResponseHeader(), DelegateTransactionDto(tx))
    }

}
