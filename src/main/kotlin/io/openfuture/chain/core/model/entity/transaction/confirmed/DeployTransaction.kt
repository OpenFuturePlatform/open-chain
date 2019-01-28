package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.TransactionFooter
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.DeployTransactionPayload
import io.openfuture.chain.network.message.core.DeployTransactionMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "deploy_transactions")
class DeployTransaction(
    header: TransactionHeader,
    footer: TransactionFooter,
    block: MainBlock,

    @Embedded
    val payload: DeployTransactionPayload

) : Transaction(header, footer, payload, block) {

    companion object {
        fun of(message: DeployTransactionMessage, block: MainBlock): DeployTransaction = DeployTransaction(
            TransactionHeader(message.timestamp, message.fee, message.senderAddress),
            TransactionFooter(message.hash, message.senderSignature, message.senderPublicKey),
            block,
            DeployTransactionPayload(message.bytecode)
        )
    }


    override fun toMessage(): DeployTransactionMessage = DeployTransactionMessage(
        header.timestamp,
        header.fee,
        header.senderAddress,
        footer.hash,
        footer.senderSignature,
        footer.senderPublicKey,
        payload.bytecode
    )

}