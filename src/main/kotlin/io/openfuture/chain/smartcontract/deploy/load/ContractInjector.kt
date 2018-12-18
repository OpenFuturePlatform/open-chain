package io.openfuture.chain.smartcontract.deploy.load

import io.openfuture.chain.smartcontract.core.model.SmartContract
import io.openfuture.chain.smartcontract.deploy.utils.SerializationUtils
import org.apache.commons.lang3.reflect.FieldUtils

class ContractInjector(
    private val instance: SmartContract,
    private val classLoader: ClassLoader
) {

    companion object {
        private const val ADDRESS_FIELD = "address"
        private const val OWNER_FIELD = "owner"
    }


    fun injectFields(address: String, owner: String): SmartContract {
        FieldUtils.writeField(instance, ADDRESS_FIELD, address, true)
        FieldUtils.writeField(instance, OWNER_FIELD, owner, true)
        return instance
    }


    fun injectState(state: ByteArray): SmartContract {
        if (state.isEmpty()) {
            return instance
        }

        val deserialized = SerializationUtils.deserialize<SmartContract>(state, classLoader)
        deserialized.javaClass.declaredFields.forEach {
            FieldUtils.writeField(instance, it.name, FieldUtils.readField(deserialized, it.name, true), true)
        }

        return instance
    }

}