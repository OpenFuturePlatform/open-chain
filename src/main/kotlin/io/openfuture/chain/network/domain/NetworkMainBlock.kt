package io.openfuture.chain.network.domain

import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction

class NetworkMainBlock() : NetworkBlock() {

    constructor(block: MainBlock) : this() {
        this.height = block.height
        this.previousHash = block.previousHash
        this.merkleHash = block.merkleHash
        this.timestamp = block.timestamp
        this.typeId = block.typeId
        this.hash = block.hash
        this.signature = block.signature!!
        this.transactions = block.transactions.map {
            if (it is TransferTransaction) NetworkTransferTransaction(it)
            else NetworkVoteTransaction(it as VoteTransaction)
        }.toMutableList()
    }
}
