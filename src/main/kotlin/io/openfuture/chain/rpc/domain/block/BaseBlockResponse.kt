package io.openfuture.chain.rpc.domain.block

abstract class BaseBlockResponse(
    var timestamp: Long,
    var height: Long,
    var previousHash: String,
    var reward: Long,
    var hash: String,
    var signature: String,
    var publicKey: String
)