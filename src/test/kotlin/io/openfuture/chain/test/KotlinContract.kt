package io.openfuture.chain.test

import io.openfuture.chain.smartcontract.model.SmartContract

class KotlinContract : SmartContract() {

    override fun execute() {
        println("Hello world")
    }

    fun another(){
        println("another method")
    }

}