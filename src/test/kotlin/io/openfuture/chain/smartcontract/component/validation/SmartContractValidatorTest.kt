package io.openfuture.chain.smartcontract.component.validation

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class SmartContractValidatorTest {

    @Test
    fun validateTest() {
        val bytes = this::class.java.getResourceAsStream("/classes/JavaContract.class").readBytes()

        val result = SmartContractValidator.validate(bytes)

        assertThat(result).isTrue()
    }

    @Test
    fun validateWhenClassIsNotSmartContractShouldReturnFalse() {
        val bytes = this::class.java.getResourceAsStream("/classes/HelloClass.class").readBytes()

        val result = SmartContractValidator.validate(bytes)

        assertThat(result).isFalse()
    }

    @Test
    fun validateWhenFieldIsNotCorrectTypeShouldReturnFalse() {
        val bytes = this::class.java.getResourceAsStream("/classes/JavaContractField.class").readBytes()

        val result = SmartContractValidator.validate(bytes)

        assertThat(result).isFalse()
    }

}