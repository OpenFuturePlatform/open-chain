package io.openfuture.chain.core.model.entity.delegate

class ViewDelegate(
    var publicKey: String,
    var nodeId: String,
    var address: String,
    var host: String,
    var port: Int,
    var registrationDate: Long,
    var rating: Long,
    var votesCount: Long
)