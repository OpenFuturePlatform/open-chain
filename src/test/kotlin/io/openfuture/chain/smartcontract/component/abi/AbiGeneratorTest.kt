package io.openfuture.chain.smartcontract.component.abi

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class AbiGeneratorTest {

    @Test
    fun generateTest() {
        val bytes = this::class.java.getResourceAsStream("/classes/KotlinContract.class").readBytes()

        val abi = AbiGenerator.generate(bytes)

        assertThat(abi).isEqualTo("""["execute","another"]""")
    }

}