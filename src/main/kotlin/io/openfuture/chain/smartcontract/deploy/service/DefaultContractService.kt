package io.openfuture.chain.smartcontract.deploy.service

import io.openfuture.chain.smartcontract.deploy.domain.ClassSource
import io.openfuture.chain.smartcontract.deploy.load.SourceClassLoader
import org.springframework.stereotype.Service

@Service
class DefaultContractService : ContractService {

    private val classLoader = SourceClassLoader()


    override fun deploy(bytes: ByteArray) {
        classLoader.loadBytes(ClassSource(bytes))
    }

    override fun run(className: String, method: String, vararg params: Any) {
        //run a method of a contract in separate thread
        TODO("not implemented")
    }

}