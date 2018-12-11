package io.openfuture.chain.smartcontract.core.model

import io.openfuture.chain.smartcontract.core.exception.RequiredException
import io.openfuture.chain.smartcontract.core.utils.AddressUtils

abstract class SmartContract(ownerAddress: String) {

    protected val owner: Address = Address(ownerAddress)
    //TODO calculate from owner address (Keccak256 hash of sender address and nonce)
    protected var address: Address = Address(AddressUtils.generateContractAddress(ownerAddress, "0"))


    protected fun required(value: Boolean, message: String? = null) {
        if (!value) {
            throw RequiredException(message)
        }
    }

}
