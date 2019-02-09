package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.network.message.core.DelegateStateMessage
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
import kotlin.Long.Companion.SIZE_BYTES

@Entity
@Table(name = "delegate_states")
class DelegateState(
    address: String,

    @Column(name = "rating", nullable = false)
    val rating: Long,

    @Column(name = "wallet_address", nullable = false)
    var walletAddress: String,

    @Column(name = "create_date", nullable = false)
    var createDate: Long,

    hash: String,
    block: Block? = null
) : State(address, hash, block) {

    constructor(address: String, walletAddress: String, createDate: Long, rating: Long = 0) : this(
        address,
        rating,
        walletAddress,
        createDate,
        lazy {
            val bytes = ByteBuffer.allocate(address.toByteArray().size + SIZE_BYTES +
                walletAddress.toByteArray().size + SIZE_BYTES)
                .put(address.toByteArray())
                .putLong(rating)
                .put(walletAddress.toByteArray())
                .putLong(createDate)
                .array()

            ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
        }.value
    )

    companion object {
        fun of(message: DelegateStateMessage, block: MainBlock? = null): DelegateState =
            DelegateState(message.address, message.rating, message.walletAddress, message.createDate, message.hash, block)
    }


    override fun getBytes(): ByteArray = ByteBuffer.allocate(address.toByteArray().size + SIZE_BYTES +
        walletAddress.toByteArray().size + SIZE_BYTES)
        .put(address.toByteArray())
        .putLong(rating)
        .put(walletAddress.toByteArray())
        .putLong(createDate)
        .array()

    override fun toMessage(): DelegateStateMessage = DelegateStateMessage(address, hash, rating, walletAddress, createDate)

}