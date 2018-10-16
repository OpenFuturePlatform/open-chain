package io.openfuture.chain.smartcontract

import io.openfuture.chain.smartcontract.annotation.ContractConstruct
import io.openfuture.chain.smartcontract.annotation.ContractMethod
import io.openfuture.chain.smartcontract.model.Address
import io.openfuture.chain.smartcontract.model.Message
import io.openfuture.chain.smartcontract.model.SmartContract

class MyTokenSmartContract : SmartContract("") {

    private val values: MutableMap<String, Long> = mutableMapOf()
    private lateinit var ticker: String

    @ContractConstruct
    fun init() {
        values[toString()] = 100000000
        ticker = "YSD"
    }

    override fun handle(message: Message) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ContractMethod
    fun balanceOf(address: Address): Long {
        return values[address.toString()] ?: 0
    }

}