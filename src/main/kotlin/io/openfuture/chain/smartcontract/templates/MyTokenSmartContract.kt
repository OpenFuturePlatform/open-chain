package io.openfuture.chain.smartcontract.templates

import io.openfuture.chain.smartcontract.core.annotation.ContractConstruct
import io.openfuture.chain.smartcontract.core.annotation.ContractMethod
import io.openfuture.chain.smartcontract.core.model.Address
import io.openfuture.chain.smartcontract.core.model.SmartContract

class MyTokenSmartContract : SmartContract() {

    private val values: MutableMap<String, Long> = mutableMapOf()
    private lateinit var ticker: String

    @ContractConstruct
    fun init() {
        values[toString()] = 100000000
        ticker = "YSD"
    }

    @ContractMethod
    fun balanceOf(address: Address): Long = values[address.toString()] ?: 0

}