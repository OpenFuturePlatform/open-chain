package io.openfuture.chain.smartcontract.core.model

import io.openfuture.chain.smartcontract.core.exception.RequiredException
import java.io.Serializable

abstract class SmartContract : Serializable {

    lateinit var owner: String
    lateinit var address: String


    protected fun required(value: Boolean, message: String? = null) {
        if (!value) {
            throw RequiredException(message)
        }
    }

}
