package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.service.transaction.DefaultSmartContractTransactionService
import io.openfuture.chain.rpc.domain.transaction.request.DeployTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.DeployTransactionResponse
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions/smartcontract")
class SmartContractTransactionController(
        private val transactionService: DefaultSmartContractTransactionService
) {

    @PostMapping
    fun add(@Valid @RequestBody request: DeployTransactionRequest): DeployTransactionResponse {
        val tx = transactionService.add(request)
        return DeployTransactionResponse(tx)
    }

}