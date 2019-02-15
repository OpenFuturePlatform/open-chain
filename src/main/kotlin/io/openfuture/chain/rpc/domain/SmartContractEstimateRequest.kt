package io.openfuture.chain.rpc.domain

import io.openfuture.chain.rpc.validation.annotation.Bytecode
import javax.validation.constraints.NotBlank

data class SmartContractEstimateRequest(
    @field:NotBlank @field:Bytecode var bytecode: String? = null
)