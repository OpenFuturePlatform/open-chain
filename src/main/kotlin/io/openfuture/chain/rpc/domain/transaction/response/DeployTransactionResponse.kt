package io.openfuture.chain.rpc.domain.transaction.response

import io.openfuture.chain.core.model.entity.transaction.confirmed.DeployTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDeployTransaction

class DeployTransactionResponse(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,
    hash: String,
    blockHash: String? = null
) : BaseTransactionResponse(timestamp, fee, senderAddress, senderSignature, senderPublicKey, hash, blockHash) {

    constructor(tx: UnconfirmedDeployTransaction) : this(
        tx.header.timestamp,
        tx.header.fee,
        tx.header.senderAddress,
        tx.footer.senderSignature,
        tx.footer.senderPublicKey,
        tx.footer.hash
    )

    constructor(tx: DeployTransaction) : this(
        tx.header.timestamp,
        tx.header.fee,
        tx.header.senderAddress,
        tx.footer.senderSignature,
        tx.footer.senderPublicKey,
        tx.footer.hash,
        tx.block.hash
    )

}