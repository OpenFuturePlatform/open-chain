package io.openfuture.chain.smartcontract.execution

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.core.model.entity.Contract
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.core.util.SerializationUtils
import io.openfuture.chain.smartcontract.component.SmartContractInjector
import io.openfuture.chain.smartcontract.model.SmartContract
import io.openfuture.chain.smartcontract.property.ContractProperties
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

class ContractExecutorTests : ServiceTests() {

    @Mock private lateinit var contractService: ContractService

    private val contractProperties: ContractProperties = ContractProperties(10000, 3, 500)

    private lateinit var executor: ContractExecutor


    @Before
    fun setUp() {
        executor = ContractExecutor(contractProperties, contractService)
    }

    @Test
    fun executeShouldProduceReceiptWithFiveResults() {
        val compiledContract = this.javaClass.classLoader.getResourceAsStream("classes/FundSmartContract.class").readBytes()
        val persistedContract = createContract(compiledContract)
        val tx = createTransferTransaction()
        val contract = SmartContractInjector.initSmartContract(FundSmartContract::class.java, "owner", "0xb0")
        val serializedContract = ByteUtils.toHexString(SerializationUtils.serialize(contract))

        given(contractService.getByAddress("contractAddress")).willReturn(persistedContract)

        val result = executor.run(serializedContract, tx, "delegateAddress")

        assertThat(result.receipt.size).isEqualTo(6)
        assertThat(result.receipt.last().amount).isEqualTo(45)
    }

    @Test
    fun executeShouldFailOnTimeout() {
        val compiledContract = this.javaClass.classLoader.getResourceAsStream("classes/TimeConsumingContract.class").readBytes()
        val persistedContract = createContract(compiledContract)
        val tx = createTransferTransaction()
        val contract = SmartContractInjector.initSmartContract(TimeConsumingContract::class.java, "ownerAddress",
            "contractAddress")
        val serializedContract = ByteUtils.toHexString(SerializationUtils.serialize(contract))

        given(contractService.getByAddress("contractAddress")).willReturn(persistedContract)

        val result = executor.run(serializedContract, tx, "delegateAddress")

        assertThat(result.receipt.size).isEqualTo(2)
        assertThat(result.receipt.first().error).isNotNull()
        assertThat(result.receipt.last().amount).isEqualTo(45)
    }

    @Test
    fun executeShouldFailOnContractExecution() {
        val compiledContract = this.javaClass.classLoader.getResourceAsStream("classes/FailingContract.class").readBytes()
        val persistedContract = createContract(compiledContract)
        val tx = createTransferTransaction()
        val contract = SmartContractInjector.initSmartContract(FailingContract::class.java, "ownerAddress",
            "contractAddress")
        val serializedContract = ByteUtils.toHexString(SerializationUtils.serialize(contract))

        given(contractService.getByAddress("contractAddress")).willReturn(persistedContract)

        val result = executor.run(serializedContract, tx, "delegateAddress")

        assertThat(result.receipt.size).isEqualTo(2)
        assertThat(result.receipt.first().error).isNotNull()
        assertThat(result.receipt.last().amount).isEqualTo(45)
    }

    private fun createTransferTransaction(): TransferTransaction = TransferTransaction(
        System.currentTimeMillis(),
        100,
        "userAddress",
        "hash",
        "sign",
        "pubKey",
        TransferTransactionPayload(
            100,
            "contractAddress",
            "execute"
        )
    )

    private fun createContract(compiledContract: ByteArray): Contract = Contract(
        "contractAddress",
        "contractOwner",
        ByteUtils.toHexString(compiledContract),
        "abi",
        55
    )

}

class FundSmartContract : SmartContract() {

    override fun execute() {
        super.transfer("0x175189F2C52E5648e7A524b7574F609D457175Cd", StrictMath.ceil(0.1 * super.getAmount()).toLong())
        super.transfer("0xaDDb12916254ACd73aD6BC45b91F0c0AB3971329", StrictMath.ceil(0.5 * super.getAmount()).toLong())
        super.transfer("0x15393B550E79b6158f65Be4901e3fC81a89cD2Bc", StrictMath.ceil(0.3 * super.getAmount()).toLong())
    }

}

class TimeConsumingContract : SmartContract() {

    override fun execute() {
        Thread.sleep(1000)
    }

}

class FailingContract : SmartContract() {

    override fun execute() {
        require(false)
    }

}