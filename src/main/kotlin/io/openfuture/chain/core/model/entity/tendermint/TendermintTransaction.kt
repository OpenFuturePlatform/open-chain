package io.openfuture.chain.core.model.entity.tendermint

import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import javax.persistence.Entity
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.Table

@Entity
@Table(name = "tendermint_transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class TendermintTransaction (
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    signature: String,
    publicKey: String
) : BaseTransaction(timestamp, fee, senderAddress, hash, signature, publicKey)