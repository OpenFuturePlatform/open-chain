package io.openfuture.chain.smartcontract.deploy.service

interface ContractService {

    fun deploy(bytes: ByteArray)

    fun callMethod(txSender: String, contractAddress: String, methodName: String, vararg params: Any)

}