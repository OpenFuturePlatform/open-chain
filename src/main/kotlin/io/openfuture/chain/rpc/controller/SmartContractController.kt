package io.openfuture.chain.rpc.controller

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
    private val contractCostCalculator: ContractCostCalculator
) {

    @PostMapping("/estimation")
    fun getEstimation(@RequestBody @Valid request: SmartContractEstimateRequest): Long =
        contractCostCalculator.calculateCost(fromHexString(request.bytecode))

}