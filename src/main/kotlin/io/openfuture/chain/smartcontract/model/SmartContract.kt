package io.openfuture.chain.smartcontract.model

import io.openfuture.chain.smartcontract.exception.RequiredException
import io.openfuture.chain.smartcontract.utils.AddressUtils

abstract class SmartContract(ownerAddress: String) {

    protected val owner: Address = Address(ownerAddress)
    //TODO calculate from owner address (Keccak256 hash of sender address and nonce)
    protected var address: Address = Address(AddressUtils.generateContractAddress(ownerAddress, "0"))


    protected fun transfer(address: String, amount: Long) {
        transfer(Address(address), amount)
    }

    protected fun transfer(address: Address, amount: Long) {
        TODO()
    }

    protected fun required(value: Boolean, message: String? = null) {
        if (!value) {
            throw RequiredException(message)
        }
    }

}
