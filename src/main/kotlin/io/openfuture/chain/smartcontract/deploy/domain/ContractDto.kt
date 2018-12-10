package io.openfuture.chain.smartcontract.deploy.domain

class ContractDto(
    val address: String,
    val owner: String,
    val state: ByteArray,
    val bytes: ByteArray,
    val clazz: String
)