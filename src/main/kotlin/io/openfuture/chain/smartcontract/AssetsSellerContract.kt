package io.openfuture.chain.smartcontract

import io.openfuture.chain.smartcontract.annotation.ContractMethod
import io.openfuture.chain.smartcontract.model.Address
import io.openfuture.chain.smartcontract.model.Message
import io.openfuture.chain.smartcontract.model.SmartContract
import io.openfuture.chain.smartcontract.utils.EventUtils
import io.openfuture.chain.smartcontract.utils.UuidUtils


class AssetsSellerContract : SmartContract("") {

    private val assets: MutableMap<String, Address> = mutableMapOf()
    private val price: Long = 1L

    override fun handle(message: Message) {
        TODO()
    }

    @ContractMethod
    fun generateAsset(message: Message, params: Map<String, String>) {
        assert(message.value == price, "Insufficient funds.")

        val uuid = UuidUtils.getRandomUuid()
        assets[uuid] = message.sender

        EventUtils.emit(this, mapOf(
            "operation" to "boughtAsset",
            "asset" to uuid,
            "buyer" to message.sender.toString()
        ))
    }


    @ContractMethod
    fun balanceOf(message: Message): List<String> {
        return assets.filterValues { it == message.sender }.keys.toList()
    }


}