package io.openfuture.chain.core.model.entity.delegate

class ViewDelegate(
    var publicKey: String = "",
    var nodeId: String = "",
    var address: String = "",
    var host: String = "",
    var port: Int = 0,
    var registrationDate: Long = 0L,
    var rating: Long = 0L,
    var votesCount: Long = 0L
)