package io.openfuture.chain.smartcontract

import io.openfuture.chain.smartcontract.model.SmartContract

class FundSmartContract: SmartContract() {

    override fun execute() {
        super.transfer("0x175189F2C52E5648e7A524b7574F609D457175Cd", StrictMath.ceil(0.1 * super.getAmount()).toLong())
        super.transfer("0xaDDb12916254ACd73aD6BC45b91F0c0AB3971329", StrictMath.ceil(0.5 * super.getAmount()).toLong())
        super.transfer("0x15393B550E79b6158f65Be4901e3fC81a89cD2Bc", StrictMath.ceil(0.3 * super.getAmount()).toLong())
    }

}