package io.openfuture.chain.smartcontract.component.load

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class SmartContractLoaderTest {

    @Test
    fun loadClassTest() {
        val bytes = this::class.java.getResourceAsStream("/classes/JavaContract.class").readBytes()

        val clazz = SmartContractLoader(this::class.java.classLoader).loadClass(bytes)

        assertThat(clazz.name).isEqualTo("io.openfuture.chain.test.JavaContract")
    }

}