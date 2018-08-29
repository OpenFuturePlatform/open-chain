package io.openfuture.chain.core.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotNull

@Component
@Validated
@ConfigurationProperties(value = "core")
class CoreProperties {

    /** The max count of transactions in block */
    @field:NotNull
    var blockTransactionsCount: Int? = null

}