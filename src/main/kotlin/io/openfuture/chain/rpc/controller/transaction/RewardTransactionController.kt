package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.service.RewardTransactionService
import io.openfuture.chain.crypto.annotation.AddressChecksum
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.transaction.request.TransactionPageRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@Validated
@RequestMapping("/rpc/transactions/reward")
class RewardTransactionController(
    val service: RewardTransactionService
) {

    @GetMapping("/{address}")
    fun getAllByRecipientAddress(@PathVariable @AddressChecksum address: String): List<RewardTransaction> =
        service.getByRecipientAddress(address)

    @GetMapping
    fun getAll(@Valid request: TransactionPageRequest): PageResponse<RewardTransaction> = PageResponse(service.getAll(request))

}