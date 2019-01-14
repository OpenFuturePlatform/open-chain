package io.openfuture.chain.smartcontract.deploy.execution

import io.openfuture.chain.ResourceUtils.getResourceBytes
import io.openfuture.chain.config.ContractTests
import io.openfuture.chain.smartcontract.core.model.SmartContract
import io.openfuture.chain.smartcontract.deploy.ContractProperties
import io.openfuture.chain.smartcontract.deploy.domain.ContractDto
import io.openfuture.chain.smartcontract.deploy.domain.ContractMethod
import io.openfuture.chain.smartcontract.deploy.exception.ContractExecutionException
import io.openfuture.chain.smartcontract.deploy.utils.SerializationUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock

class ContractExecutorTest : ContractTests() {

    @Mock private lateinit var contractProperties: ContractProperties

    @InjectMocks
    private lateinit var executor: ContractExecutor


    @Test
    fun executeWhenValidMethodShouldReturnResult() {
        val bytes = getResourceBytes("/classes/HelloContract.class")
        val clazz = "io.test.HelloClass"
        val contractAddress = "0xAdDrEsS"
        val contractOwner = "0xOwNeR"

        given(contractProperties.executionTimeout).willReturn(1000)

        val result = executor.run(
            ContractDto(contractOwner, contractAddress, ByteArray(0), bytes, clazz),
            ContractMethod("helloFromOwner")
        )

        assertThat(result).isNotNull
        assertThat(result.instance).isNotNull
        assertThat(result.instance).isInstanceOf(SmartContract::class.java)
        assertThat(result.output).isNotNull
        assertThat(result.output).isEqualTo("Hello from $contractAddress ($contractOwner)")
    }

    @Test(expected = ContractExecutionException::class)
    fun executeWhenMethodExecutionTakesTooLongShouldThrowContractExecutionException() {
        val bytes = getResourceBytes("/classes/HelloContract.class")

        given(contractProperties.executionTimeout).willReturn(10)

        executor.run(
            ContractDto("0xOwNeR", "0xAdDrEsS", ByteArray(0), bytes, "io.test.HelloClass"),
            ContractMethod("helloSleep")
        )
    }

    @Test
    fun executeWhenStateChangesShouldRestorePrevState() {
        val bytes = getResourceBytes("/classes/CalculatorContract.class")
        val clazz = "io.openfuture.chain.smartcontract.templates.CalculatorContract"
        val contractAddress = "0xAdDrEsS"
        val contractOwner = "0xOwNeR"

        given(contractProperties.executionTimeout).willReturn(10000)

        var result = executor.run(
            ContractDto(contractAddress, contractOwner, ByteArray(0), bytes, clazz),
            ContractMethod("add", arrayOf(20L))
        )

        result = executor.run(
            ContractDto(contractAddress, contractOwner, SerializationUtils.serialize(result.instance!!), bytes, clazz),
            ContractMethod("result")
        )

        assertThat(result).isNotNull
        assertThat(result.output).isEqualTo(20L)
    }

}