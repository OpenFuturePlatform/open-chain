package io.openfuture.chain.smartcontract.deploy.service

//@Service
//class DefaultContractService(
//    private val executor: ContractExecutor
//) : ContractService {
//
//    private val classLoader = SourceClassLoader()
//
//
//    override fun deploy(bytes: ByteArray) {
//        classLoader.loadBytes(ClassSource(bytes))
//    }
//
//    //todo replace with transaction
//    override fun callMethod(contractAddress: String, methodName: String, vararg params: Any) {
//        val contact = repository.get(contractAddress)
//
//        //todo validateIsMethodExists()
//        val method = ContractMethod(methodName, params)
//
//        val result = executor.run(contact, method)
//        contact.state = SerializationUtils.serialize(result.instance!!)
//        repository.save(contact)
//    }
//
//}