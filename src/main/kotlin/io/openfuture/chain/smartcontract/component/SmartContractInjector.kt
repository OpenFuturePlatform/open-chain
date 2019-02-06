package io.openfuture.chain.smartcontract.component

import io.openfuture.chain.smartcontract.model.SmartContract
import org.springframework.util.ReflectionUtils

object SmartContractInjector {

    private const val OWNER_FIELD = "owner"
    private const val ADDRESS_FIELD = "address"


    fun initSmartContract(clazz: Class<*>, owner: String, address: String): SmartContract {
        val instance = clazz.newInstance() as SmartContract

        injectField(instance, OWNER_FIELD, owner)
        injectField(instance, ADDRESS_FIELD, address)

        return instance
    }

    private fun injectField(instance: SmartContract, fieldName: String, value: String) {
        val field = ReflectionUtils.findField(instance::class.java, fieldName)
        ReflectionUtils.makeAccessible(field!!)
        ReflectionUtils.setField(field, instance, value)
    }

}