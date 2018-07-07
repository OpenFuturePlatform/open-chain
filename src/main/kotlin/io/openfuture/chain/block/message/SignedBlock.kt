package io.openfuture.chain.block.message

import io.openfuture.chain.entity.Block

data class SignedBlock(

    val block: Block,

    val signature: String

)