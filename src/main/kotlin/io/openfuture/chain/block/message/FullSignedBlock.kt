package io.openfuture.chain.block.message

import io.openfuture.chain.entity.Block

data class FullSignedBlock(

    val block: Block,

    val signatures: Set<String>

)