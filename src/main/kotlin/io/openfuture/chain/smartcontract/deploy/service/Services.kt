package io.openfuture.chain.smartcontract.deploy.service

interface ContractService {

    fun deploy(bytes: ByteArray)

    fun run(className: String, method: String, vararg params: Any)

}