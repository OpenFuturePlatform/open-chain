package io.openfuture.chain.core.model.entity.block.payload

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import javax.persistence.*

@Embeddable
class GenesisBlockPayload(
    previousHash: String,
    reward: Long,

    @Column(name = "epoch_index", nullable = false)
    var epochIndex: Long,

    @ManyToMany(fetch = FetchType.EAGER, cascade = [(CascadeType.ALL)])
    @JoinTable(name = "delegate2genesis",
        joinColumns = [(JoinColumn(name = "genesis_id"))],
        inverseJoinColumns = [(JoinColumn(name = "delegate_id"))])
    var activeDelegates: Set<Delegate> = mutableSetOf()

) : BaseBlockPayload(previousHash, reward) {

    override fun getBytes() : ByteArray {
        val builder = StringBuilder()
        builder.append(previousHash)
        builder.append(reward)
        builder.append(epochIndex)
        builder.append(activeDelegates)
        return builder.toString().toByteArray()
    }

}