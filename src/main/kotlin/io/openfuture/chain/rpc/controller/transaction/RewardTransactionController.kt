package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.service.RewardTransactionService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rpc/transactions/reward")
class RewardTransactionController(
    val service: RewardTransactionService
) {

    @GetMapping("/{address}")
    fun getAllByRecipientAddress(@PathVariable address: String): List<RewardTransaction> =
        service.getByRecipientAddress(address)

    @GetMapping
    fun getAll(request: PageRequest): PageResponse<RewardTransaction> = PageResponse(service.getAll(request))

}