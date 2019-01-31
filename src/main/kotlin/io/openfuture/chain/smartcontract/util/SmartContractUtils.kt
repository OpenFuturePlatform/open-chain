package io.openfuture.chain.smartcontract.util

import io.openfuture.chain.smartcontract.core.model.SmartContract
import io.openfuture.chain.smartcontract.exception.SmartContractClassCastException
import org.springframework.util.ReflectionUtils

object SmartContractUtils {

    private const val OWNER_FIELD = "owner"
    private const val ADDRESS_FIELD = "address"


    fun initSmartContract(clazz: Class<*>, owner: String, address: String): SmartContract {
        val instance = clazz.newInstance()
        injectField(instance, OWNER_FIELD, owner)
        injectField(instance, ADDRESS_FIELD, address)

        return instance as? SmartContract ?: throw SmartContractClassCastException("Instatnce has invalid type")
    }

    private fun injectField(instance: Any, fieldName: String, value: String) {
        val field = ReflectionUtils.findField(instance::class.java, fieldName)
            ?: throw SmartContractClassCastException("Instatnce has invalid type")
        ReflectionUtils.makeAccessible(field)
        ReflectionUtils.setField(field, instance, value)
    }

}