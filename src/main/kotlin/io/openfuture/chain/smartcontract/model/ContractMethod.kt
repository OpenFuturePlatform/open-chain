package io.openfuture.chain.smartcontract.model

class ContractMethod(
    private val name: String,
    private val returnType: ContractParameterType,
    private val parameterTypes: Array<ContractParameterType>
)