package io.openfuture.chain.consensus.property

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
    var timeSlotDuration: Long? = null,

    /**
     * Time between time slot
     */
    @field:NotNull
    var timeSlotInterval: Long? =null,

    /**
     * Genesis address of blockchain
     */
    @field:NotNull
    var genesisAddress: String? = null,

    /**
     * Reward for block
     */
    @field:NotNull
    var rewardBlock: Long? = null,

    /**
     * Fee of vote transaction for delegate
     */
    @field:NotNull
    var feeVoteTxFor: Long? = null,

    /**
     * Fee of vote transaction against delegate
     */
    @field:NotNull
    var feeVoteTxAgainst: Long? = null,

    /**
     * Fee for delegate transaction
     */
    @field:NotNull
    var feeDelegateTx: Long? = null,

    /**
     * Amount for delegate transaction
     */
    @field:NotNull
    var amountDelegateTx: Long? = null

) {

    fun getPeriod(): Long = timeSlotDuration!! + timeSlotInterval!!

}