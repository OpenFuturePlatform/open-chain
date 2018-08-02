package io.openfuture.chain.network.domain

abstract class NetworkBlock(
    var height: Long,
    var previousHash: String,
    var timestamp: Long,
    var reward: Long,
    var hash: String? = null,
    var publicKey: String? = null,
    var signature: String? = null
) : Packet() {

    abstract fun getBytes(): ByteArray

}