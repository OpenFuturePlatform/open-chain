package io.openfuture.chain.domain.block

import io.openfuture.chain.domain.transaction.TransactionRequest

class GenesisBlock private constructor() {

    companion object {
        val instance = BlockRequest(2, 0, 1, "previous_hash",
                "genesis_private_key", "genesis_public_key",
                listOf(TransactionRequest(0, 0, "genesis_recipient_key",
                        "genesis_sender_key", "genesis_signature")))
    }

}