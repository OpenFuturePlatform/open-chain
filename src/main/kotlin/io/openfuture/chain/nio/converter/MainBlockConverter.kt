package io.openfuture.chain.nio.converter

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component

@Component
class MainBlockConverter(
    private val transactionConverter: TransactionConverter
): MessageConverter<Block,  CommunicationProtocol.MainBlock> {

    private val mainBlockBuilder = CommunicationProtocol.MainBlock.newBuilder()


    override fun fromMessage(message: CommunicationProtocol.MainBlock): MainBlock {
        return MainBlock(
            message.hash,
            message.height,
            message.previousHash,
            message.merkleHash,
            message.timestamp,
            message.signature,
            message.transactionsList.map { transactionConverter.fromMessage(it) }.toList())
    }

    override fun fromEntity(entity: Block): CommunicationProtocol.MainBlock {
        val mainBlock = entity as MainBlock
        return mainBlockBuilder
            .setHash(mainBlock.hash)
            .setHeight(mainBlock.height)
            .setPreviousHash(mainBlock.previousHash)
            .setMerkleHash(mainBlock.merkleHash)
            .setTimestamp(mainBlock.timestamp)
            .setSignature(mainBlock.signature)
            .setTypeId(mainBlock.typeId)
            .addAllTransactions(mainBlock.transactions.map { transactionConverter.fromEntity(it) }.toList())
            .build()
    }

}