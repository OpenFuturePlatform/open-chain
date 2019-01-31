package io.openfuture.chain.smartcontract.util

import io.openfuture.chain.smartcontract.component.SmartContractUtils
import io.openfuture.chain.smartcontract.component.load.SmartContractLoader
import io.openfuture.chain.smartcontract.exception.SmartContractClassCastException
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class SmartContractUtilsTest {

    @Test
    fun initSmartContractTest() {
        val owner = "owner"
        val address = "address"
        val bytes = this::class.java.getResourceAsStream("/classes/HelloContract.class").readBytes()
        val clazz = SmartContractLoader().loadClass(bytes)

        val smartContract = SmartContractUtils.initSmartContract(clazz, owner, address)

        assertThat(smartContract.owner).isEqualTo(owner)
        assertThat(smartContract.address).isEqualTo(address)
    }

    @Test(expected = SmartContractClassCastException::class)
    fun initSmartContractWhenInstanceHasInvalidType() {
        val owner = "owner"
        val address = "address"

        SmartContractUtils.initSmartContract(SmartContractUtilsTest::class.java, owner, address)
    }

}