package io.openfuture.chain.smartcontract.model

abstract class Event {

    abstract fun parameters(): Map<String, Any>

    fun emit() {
        val params = parameters()
        TODO()
    }

}