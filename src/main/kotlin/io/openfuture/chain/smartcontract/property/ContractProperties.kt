package io.openfuture.chain.smartcontract.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotNull

@Component
@Validated
@ConfigurationProperties(prefix = "contract")
class ContractProperties(

    /** Timeout for contract method execution */
    @field:NotNull
    var executionTimeout: Long? = null,

    @field:NotNull
    var millisecondCost: Long? = null,

    @field:NotNull
    var maxExecutionTime: Long? = null

)