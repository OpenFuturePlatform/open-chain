package io.openfuture.chain.smartcontract.model

import io.openfuture.chain.smartcontract.exception.AssertionException
import io.openfuture.chain.smartcontract.exception.RequiredException

abstract class SmartContract(address: String) : Address(address) {

    protected lateinit var owner: Address

    protected fun transfer(address: String, amount: Long) {
        transfer(Address(address), amount)
    }

    protected fun transfer(address: Address, amount: Long) {
        TODO()
    }

    /**
     * Default method.
     */
    abstract fun handle(message: Message)

    protected fun assert(value: Boolean, message: String? = null) {
        if (!value) {
            throw AssertionException(message)
        }
    }

    protected fun required(arg: String?, message: String? = null) {
        if (arg == null) {
            throw RequiredException(message)
        }
    }
}

