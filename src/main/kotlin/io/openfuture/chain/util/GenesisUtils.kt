package io.openfuture.chain.util

import io.openfuture.chain.domain.block.BlockRequest
import io.openfuture.chain.domain.transaction.TransactionRequest

class GenesisUtils {

    //todo temp constants, need to think about this
    companion object {
        val genesisTransactions = listOf(TransactionRequest(0, 0, "genesis_recipient_key",
                "genesis_sender_key", "genesis_signature"))
        val genesisBlock = BlockRequest(2, 0, 1, "previous_hash",
                "genesis_private_key", "genesis_public_key", genesisTransactions)
    }

}