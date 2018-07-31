package io.openfuture.chain.consensus.model.entity.block

import io.openfuture.chain.consensus.util.TransactionUtils
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import javax.persistence.*

@Entity
@Table(name = "main_blocks")
class MainBlock(
    height: Long,
    previousHash: String,
    timestamp: Long,
    publicKey: String,

    @OneToMany(mappedBy = "block", fetch = FetchType.EAGER)
    var transactions: MutableSet<Transaction>,

    @Column(name = "merkle_hash", nullable = false)
    var merkleHash: String = TransactionUtils.calculateMerkleRoot(transactions)

) : Block(height, previousHash, timestamp, publicKey,
    ByteUtils.toHexString(HashUtils.doubleSha256((previousHash + merkleHash + timestamp + height + publicKey).toByteArray()))) {

    override fun sign(privateKey: ByteArray): MainBlock {
        this.signature = SignatureUtils.sign(getBytes(), privateKey)
        return this
    }

    override fun getBytes(): ByteArray = (previousHash + merkleHash + timestamp + height).toByteArray()

}