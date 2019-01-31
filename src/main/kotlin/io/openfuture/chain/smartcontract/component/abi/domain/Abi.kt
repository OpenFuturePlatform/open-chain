package io.openfuture.chain.smartcontract.component.abi.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class Abi {

    private val methodNames: MutableSet<String> = mutableSetOf()


    fun getAbi(): String = jacksonObjectMapper().writeValueAsString(methodNames)

    fun addMethodName(methodName: String) {
        methodNames.add(methodName)
    }

}