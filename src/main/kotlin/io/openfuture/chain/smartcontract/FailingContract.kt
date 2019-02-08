package io.openfuture.chain.smartcontract

import io.openfuture.chain.smartcontract.model.SmartContract

class FailingContract: SmartContract() {

    override fun execute() {
        require(false)
    }

}