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
    var epochHeight: Int? = null,

    /** The count of active delegates */
    @field:NotNull
    var delegatesCount: Int? = null,

    /**
     * The count of transactions
     */
    @field:NotNull
    var blockCapacity: Int? = null,

    /**
     * Time slot duration
     */
    @field:NotNull
    var timeSlotDuration: Long? = null

)