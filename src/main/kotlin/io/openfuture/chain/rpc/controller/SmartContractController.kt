package io.openfuture.chain.rpc.controller

import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType.DEPLOY
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType.EXECUTE
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.rpc.domain.SmartContractEstimateRequest
import io.openfuture.chain.smartcontract.deploy.calculation.ContractCostCalculator
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.fromHexString
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/contracts")
class SmartContractController(
    private val contractCostCalculator: ContractCostCalculator,
    private val contractService: ContractService
) {

    @PostMapping("/estimation")
    fun getEstimation(@RequestBody @Valid request: SmartContractEstimateRequest): Long =
        when (TransferTransactionType.getType(request.recipientAddress, request.data)) {
            DEPLOY -> contractCostCalculator.calculateCost(fromHexString(request.data))
            EXECUTE -> contractService.getByAddress(request.recipientAddress!!).cost
            else -> throw IllegalArgumentException("Invalid request")
        }

}