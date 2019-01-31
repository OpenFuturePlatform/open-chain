package io.openfuture.chain.smartcontract.model

import org.apache.commons.lang3.StringUtils.EMPTY
import java.io.Serializable

abstract class SmartContract : Serializable {

    val owner: String = EMPTY
    val address: String = EMPTY


    abstract fun execute()

}
