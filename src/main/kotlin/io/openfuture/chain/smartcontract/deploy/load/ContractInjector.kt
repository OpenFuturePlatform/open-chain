package io.openfuture.chain.smartcontract.deploy.load

import io.openfuture.chain.smartcontract.core.model.SmartContract
import org.apache.commons.lang3.reflect.FieldUtils

class ContractInjector(private val instance: Any) {

    companion object {
        private const val ADDRESS_FIELD = "address"
        private const val OWNER_FIELD = "owner"
    }

    init {
        require(instance is SmartContract) { "Instance is not a smart contract" }
    }


    fun injectFields(address: String, owner: String): Any {
        FieldUtils.writeField(instance, ADDRESS_FIELD, address, true)
        FieldUtils.writeField(instance, OWNER_FIELD, owner, true)
        return instance
    }

}