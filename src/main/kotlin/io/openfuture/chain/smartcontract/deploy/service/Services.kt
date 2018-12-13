package io.openfuture.chain.smartcontract.deploy.service

interface ContractService {

    fun deploy(bytes: ByteArray)

    fun callMethod(contractAddress: String, methodName: String, vararg params: Any)

}