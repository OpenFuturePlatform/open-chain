package io.openfuture.chain.smartcontract.deploy.service

import io.openfuture.chain.smartcontract.deploy.domain.ClassSource
import io.openfuture.chain.smartcontract.deploy.domain.ContractMethod
import io.openfuture.chain.smartcontract.deploy.execution.ContractExecutor
import io.openfuture.chain.smartcontract.deploy.load.SourceClassLoader
import io.openfuture.chain.smartcontract.deploy.repository.ContractRepository
import org.springframework.stereotype.Service

@Service
class DefaultContractService(
    private val repository: ContractRepository
) : ContractService {

    private val classLoader = SourceClassLoader()
    private val executor = ContractExecutor()


    override fun deploy(bytes: ByteArray) {
        classLoader.loadBytes(ClassSource(bytes))
    }

    //todo replace with transaction
    override fun callMethod(contractAddress: String, methodName: String, vararg params: Any) {
        val contact = repository.get(contractAddress)

        //todo validateIsMethodExists()
        val method = ContractMethod(methodName, params)

        val result = executor.run(contact, method)
        //todo update state
    }

}