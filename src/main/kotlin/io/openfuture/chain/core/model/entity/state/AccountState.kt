package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.network.message.core.AccountStateMessage
import org.apache.commons.lang3.StringUtils.EMPTY
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
import kotlin.Long.Companion.SIZE_BYTES

@Entity
@Table(name = "account_states")
class AccountState(
    address: String,

    @Column(name = "balance", nullable = false)
    val balance: Long = 0,

    @Column(name = "vote_for")
    val voteFor: String? = null,

    @Column(name = "storage")
    val storage: String? = null,

    hash: String,
    block: Block? = null
) : State(address, hash, block) {

    constructor(address: String, balance: Long = 0, voteFor: String? = null, storage: String? = null) : this(
        address,
        balance,
        voteFor,
        storage,
        lazy {
            val voteForBytes = (voteFor ?: EMPTY).toByteArray()
            val storageBytes = (storage ?: EMPTY).toByteArray()

            val bytes = ByteBuffer.allocate(address.toByteArray().size + SIZE_BYTES +
                voteForBytes.size + storageBytes.size)
                .put(address.toByteArray())
                .putLong(balance)
                .put(voteForBytes)
                .put(storageBytes)
                .array()

            ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
        }.value
    )

    companion object {
        fun of(message: AccountStateMessage, block: MainBlock? = null): AccountState =
            AccountState(message.address, message.balance, message.voteFor, message.storage, message.hash, block)
    }


    override fun getBytes(): ByteArray {
        val voteForBytes = (voteFor ?: EMPTY).toByteArray()
        val storageBytes = (storage ?: EMPTY).toByteArray()

        return ByteBuffer.allocate(address.toByteArray().size + SIZE_BYTES +
            voteForBytes.size + storageBytes.size)
            .put(address.toByteArray())
            .putLong(balance)
            .put(voteForBytes)
            .put(storageBytes)
            .array()
    }

    override fun toMessage(): AccountStateMessage = AccountStateMessage(address, hash, balance, voteFor, storage)

}