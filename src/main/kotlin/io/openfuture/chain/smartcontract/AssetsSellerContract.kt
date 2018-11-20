package io.openfuture.chain.smartcontract

import io.openfuture.chain.smartcontract.annotation.ContractMethod
import io.openfuture.chain.smartcontract.model.Address
import io.openfuture.chain.smartcontract.model.Event
import io.openfuture.chain.smartcontract.model.Message
import io.openfuture.chain.smartcontract.model.SmartContract


class AssetsSellerContract : SmartContract("") {

    private val assets: MutableMap<String, Address> = mutableMapOf()
    private val price: Long = 1L


    @ContractMethod
    fun generateAsset(message: Message, params: Map<String, String>) {
        required(message.value == price, "Insufficient funds.")

        val uuid = params["uuid"]!!
        assets[uuid] = message.sender

        BoughtAssetEvent(uuid, message.sender.toString()).emit()
    }


    @ContractMethod
    fun balanceOf(message: Message): List<String> = assets.filterValues { it == message.sender }.keys.toList()


    class BoughtAssetEvent(private val asset: String, private val buyer: String) : Event() {

        override fun parameters(): Map<String, Any> = mapOf(
            "asset" to asset,
            "buyer" to buyer
        )

    }

}