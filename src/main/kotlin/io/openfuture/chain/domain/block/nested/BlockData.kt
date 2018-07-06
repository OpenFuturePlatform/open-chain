package io.openfuture.chain.domain.block.nested

data class BlockData(
        val orderNumber: Long,
        val previousHash: String,
        val merkleHash: MerkleHash
) {

    fun getByteData(): ByteArray {
        val builder = StringBuilder()
        builder.append(orderNumber)
        builder.append(previousHash)
        builder.append(merkleHash)
        return builder.toString().toByteArray()
    }

}