package io.openfuture.chain.controller

import io.openfuture.chain.controller.common.BaseController
import io.openfuture.chain.controller.common.RestResponse
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.DelegateTransactionDto
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
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
) : BaseController() {

    @PostMapping("/votes/doGenerateHash")
    fun getVoteDataHash(@Valid @RequestBody data: VoteTransactionData): RestResponse<String> {
        return RestResponse(getResponseHeader(), data.getHash())
    }

    @PostMapping("/votes")
    fun addVote(@Valid @RequestBody request: BaseTransactionRequest<VoteTransactionData>): RestResponse<VoteTransactionDto> {
        val tx = voteTransactionService.add(request)
        return RestResponse(getResponseHeader(), VoteTransactionDto(tx))
    }

    @PostMapping("/transfers/doGenerateHash")
    fun getTransferDataHash(@Valid @RequestBody data: TransferTransactionData): RestResponse<String> {
        return RestResponse(getResponseHeader(), data.getHash())
    }

    @PostMapping("/transfers")
    fun addTransfer(@Valid @RequestBody request: BaseTransactionRequest<TransferTransactionData>): RestResponse<TransferTransactionDto> {
        val tx = transferTransactionService.add(request)
        return RestResponse(getResponseHeader(), TransferTransactionDto(tx))
    }

    @PostMapping("/delegates/doGenerateHash")
    fun getDelegateDataHash(@Valid @RequestBody data: DelegateTransactionData): RestResponse<String> {
        return RestResponse(getResponseHeader(), data.getHash())
    }

    @PostMapping("/delegates")
    fun addDelegates(@Valid @RequestBody request: BaseTransactionRequest<DelegateTransactionData>): RestResponse<DelegateTransactionDto> {
        val tx = delegateTransactionService.add(request)
        return RestResponse(getResponseHeader(), DelegateTransactionDto(tx))
    }

}
