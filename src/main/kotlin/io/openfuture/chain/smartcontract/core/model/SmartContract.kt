package io.openfuture.chain.smartcontract.core.model

import io.openfuture.chain.smartcontract.core.exception.RequiredException

abstract class SmartContract {

    protected lateinit var owner: String
    protected lateinit var address: String


    protected fun required(value: Boolean, message: String? = null) {
        if (!value) {
            throw RequiredException(message)
        }
    }

}
