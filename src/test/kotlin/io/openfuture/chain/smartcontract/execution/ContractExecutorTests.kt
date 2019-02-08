package io.openfuture.chain.smartcontract.execution

import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.smartcontract.component.SmartContractInjector
import io.openfuture.chain.smartcontract.component.load.SmartContractLoader
import io.openfuture.chain.smartcontract.model.SmartContract
import io.openfuture.chain.smartcontract.property.ContractProperties
import io.openfuture.chain.smartcontract.util.SerializationUtils
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Test

class ContractExecutorTests {

    private val contractProperties = ContractProperties(10000, 3, 500)

    private val executor = ContractExecutor(contractProperties)

    @Test
    fun executeShouldProduceReceiptWithFiveResults() {
        val message = createMessage()
        val contract = SmartContractInjector.initSmartContract(FundSmartContract::class.java, "owner", "0xb0")
        val serializedContract = ByteUtils.toHexString(SerializationUtils.serialize(contract))

        val result = executor.run(serializedContract, message, "delegateAddress")

        assertThat(result.receipt.size).isEqualTo(5)
        assertThat(result.receipt.last().amount).isEqualTo(message.fee)
    }

    @Test
    fun executeShouldFailOnTimeout() {
        val message = createMessage()
        val contract = SmartContractInjector.initSmartContract(TimeConsumingContract::class.java, "ownerAddress", "contractAddress")
        val serializedContract = ByteUtils.toHexString(SerializationUtils.serialize(contract))

        val result = executor.run(serializedContract, message, "delegateAddress")

        assertThat(result.receipt.size).isEqualTo(2)
        assertThat(result.receipt.first().error).isNotNull()
        assertThat(result.receipt.last().amount).isEqualTo(message.fee)
    }

    @Test
    fun executeShouldFailOnContractExecution() {
        val message = createMessage()
        val contract = SmartContractInjector.initSmartContract(FailingContract::class.java, "ownerAddress", "contractAddress")
        val serializedContract = ByteUtils.toHexString(SerializationUtils.serialize(contract))

        val result = executor.run(serializedContract, message, "delegateAddress")

        assertThat(result.receipt.size).isEqualTo(2)
        assertThat(result.receipt.first().error).isNotNull()
        assertThat(result.receipt.last().amount).isEqualTo(message.fee)
    }

    private fun createMessage(): TransferTransactionMessage = TransferTransactionMessage(
        System.currentTimeMillis(),
        100,
        "userAddress",
        "hash",
        "sign",
        "pubKey",
        100,
        "contractAddress",
        "execute"
    )

}

class FundSmartContract: SmartContract() {

    private val distributions = hashMapOf(
        "0x175189F2C52E5648e7A524b7574F609D457175Cd" to 0.1,
        "0xaDDb12916254ACd73aD6BC45b91F0c0AB3971329" to 0.5,
        "0x15393B550E79b6158f65Be4901e3fC81a89cD2Bc" to 0.3
    )

    override fun execute() {
        distributions.forEach {
            super.transfer(it.key, StrictMath.ceil(it.value * super.getAmount()).toLong())
        }
    }

}

class TimeConsumingContract: SmartContract() {

    override fun execute() {
        Thread.sleep(1000)
    }

}

class FailingContract: SmartContract() {

    override fun execute() {
        require(false)
    }

}