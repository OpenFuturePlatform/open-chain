package io.openfuture.chain.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotNull

@Component
@Validated
@ConfigurationProperties(prefix = "consensus")
class ConsensusProperties(

    /** The count of blocks in epoch */
    @field:NotNull
    var epochHeight: Int? = null

)