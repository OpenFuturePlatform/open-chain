package io.openfuture.chain.smartcontract.component.load

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class SmartContractLoaderTest {

    @Test
    fun loadClassTest() {
        val bytes = this::class.java.getResourceAsStream("/classes/HelloContract.class").readBytes()

        val clazz = SmartContractLoader().loadClass(bytes)

        assertThat(clazz.name).isEqualTo("io.test.HelloContract")
    }

}