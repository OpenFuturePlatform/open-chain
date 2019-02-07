package io.openfuture.chain.smartcontract.component

import io.openfuture.chain.smartcontract.component.load.SmartContractLoader
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class SmartContractInjectorTest {

    @Test
    fun initSmartContractTest() {
        val owner = "owner"
        val address = "address"
        val bytes = this::class.java.getResourceAsStream("/classes/JavaContract.class").readBytes()
        val clazz = SmartContractLoader(this::class.java.classLoader).loadClass(bytes)

        val smartContract = SmartContractInjector.initSmartContract(clazz, owner, address)

        assertThat(smartContract.owner).isEqualTo(owner)
        assertThat(smartContract.address).isEqualTo(address)
    }

    @Test(expected = ClassCastException::class)
    fun initSmartContractWhenInstanceHasInvalidType() {
        val owner = "owner"
        val address = "address"

        SmartContractInjector.initSmartContract(SmartContractInjectorTest::class.java, owner, address)
    }

}