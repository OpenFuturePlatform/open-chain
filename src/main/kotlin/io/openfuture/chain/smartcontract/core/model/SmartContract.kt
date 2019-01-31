package io.openfuture.chain.smartcontract.core.model

import io.openfuture.chain.smartcontract.core.exception.RequiredException
import org.apache.commons.lang3.StringUtils.EMPTY
import java.io.Serializable

abstract class SmartContract : Serializable {

    val owner: String = EMPTY
    val address: String = EMPTY


    protected fun required(value: Boolean, message: String? = null) {
        if (!value) {
            throw RequiredException(message)
        }
    }

}
