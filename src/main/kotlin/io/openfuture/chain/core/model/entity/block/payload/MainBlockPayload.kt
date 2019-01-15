package io.openfuture.chain.core.model.entity.block.payload

import com.fasterxml.jackson.annotation.JsonIgnore
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.charset.StandardCharsets.UTF_8
import javax.persistence.*

@Embeddable
class MainBlockPayload(

    @Column(name = "merkle_hash", nullable = false)
    var merkleHash: String,

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    var rewardTransaction: MutableList<RewardTransaction> = mutableListOf(),

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    var voteTransactions: MutableList<VoteTransaction> = mutableListOf(),

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    var delegateTransactions: MutableList<DelegateTransaction> = mutableListOf(),

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    var transferTransactions: MutableList<TransferTransaction> = mutableListOf()

) : BlockPayload {

    override fun getBytes(): ByteArray = merkleHash.toByteArray(UTF_8)

    companion object {

        fun calculateMerkleRoot(transactions: List<String>): String {
            if (transactions.size == 1) {
                return transactions.single()
            }

            var previousTreeLayout = transactions.asSequence().sortedByDescending { it }.map { it.toByteArray() }.toList()
            var treeLayout = mutableListOf<ByteArray>()
            while (previousTreeLayout.size != 2) {
                for (i in 0 until previousTreeLayout.size step 2) {
                    val leftHash = previousTreeLayout[i]
                    val rightHash = if (i + 1 == previousTreeLayout.size) {
                        previousTreeLayout[i]
                    } else {
                        previousTreeLayout[i + 1]
                    }
                    treeLayout.add(HashUtils.sha256(leftHash + rightHash))
                }
                previousTreeLayout = treeLayout
                treeLayout = mutableListOf()
            }
            return ByteUtils.toHexString(HashUtils.doubleSha256(previousTreeLayout[0] + previousTreeLayout[1]))
        }
    }


}