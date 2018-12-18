package io.openfuture.chain.smartcontract.deploy.domain

class ContractDto(
    var address: String,
    var owner: String,
    var state: ByteArray,
    var bytes: ByteArray,
    var clazz: String
)