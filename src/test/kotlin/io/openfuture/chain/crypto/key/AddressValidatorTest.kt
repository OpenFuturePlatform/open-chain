package io.openfuture.chain.crypto.key

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class AddressValidatorTest {

    private val validator = AddressValidator()

    @Test
    fun validateShouldReturnTrueForValidAddress() {
        val address = "0x5aF3B0FFB89C09D7A38Fd01E42E0A5e32011e36e"

        val result = validator.validate(address)

        assertThat(result).isTrue()
    }

    @Test
    fun validateShouldReturnFalseForInvalidAddress() {
        val address = "0x5aF3B0FFB89C09D7A38Fd01E42E0A5e32011e36E"

        val result = validator.validate(address)

        assertThat(result).isFalse()
    }
}