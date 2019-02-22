package io.openfuture.chain.core.model.entity.state

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
    var balance: Long = 0,

    @Column(name = "vote_for")
    var voteFor: String? = null,

    @Column(name = "storage")
    var storage: String? = null,

    hash: String
) : State(address, hash) {

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
        fun of(message: AccountStateMessage): AccountState =
            AccountState(message.address, message.balance, message.voteFor, message.storage, message.hash)
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