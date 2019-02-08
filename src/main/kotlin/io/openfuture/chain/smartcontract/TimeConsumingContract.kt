package io.openfuture.chain.smartcontract

import io.openfuture.chain.smartcontract.model.SmartContract

class TimeConsumingContract: SmartContract() {

    override fun execute() {
        Thread.sleep(1000)
    }

}