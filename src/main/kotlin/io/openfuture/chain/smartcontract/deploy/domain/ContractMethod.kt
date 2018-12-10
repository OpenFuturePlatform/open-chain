package io.openfuture.chain.smartcontract.deploy.domain

class ContractMethod(
    val name: String,
    val params: Array<out Any> = emptyArray()
)