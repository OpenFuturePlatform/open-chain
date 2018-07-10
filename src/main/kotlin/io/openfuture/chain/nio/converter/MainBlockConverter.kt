package io.openfuture.chain.nio.converter

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component

@Component
class MainBlockConverter(
    private val transactionConverter: TransactionConverter
) {

    fun toMainBlock(mainBlock: CommunicationProtocol.MainBlock): MainBlock {
        return MainBlock(
            mainBlock.hash,
            mainBlock.height,
            mainBlock.previousHash,
            mainBlock.merkleHash,
            mainBlock.timestamp,
            mainBlock.signature,
            mainBlock.transactionsList.map { transactionConverter.toTransaction(it) }.toList())
    }

    fun toMainBlockProto(block: Block): CommunicationProtocol.MainBlock {
        val mainBlock = block as MainBlock
        return CommunicationProtocol.MainBlock.newBuilder()
            .setHash(mainBlock.hash)
            .setHeight(mainBlock.height)
            .setPreviousHash(mainBlock.previousHash)
            .setMerkleHash(mainBlock.merkleHash)
            .setTimestamp(mainBlock.timestamp)
            .setSignature(mainBlock.signature)
            .addAllTransactions(mainBlock.transactions.map { transactionConverter.toTransactionProto(it) }.toList())
            .build()
    }

}