package io.openfuture.chain.crypto.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class Base58Test {

    @Test
    fun encodeShouldReturnCorrectlyEncodedBytes() {
        val value = "Hello, world!"
        val expectedValue = "72k1xXWG59wUsYv7h2"

        val encoded = Base58.encode(value.toByteArray())

        assertThat(encoded).isEqualTo(expectedValue)
    }

    @Test
    fun encodeShouldReturnEmptyStringWhenBytesToEncodeAreEmpty() {
        val encoded = Base58.encode(ByteArray(0))

        assertThat(encoded).isEmpty()
    }

    @Test
    fun encodeWithChecksumShouldReturnCorrectlyEncodedBytes() {
        val value = "Hello, world!"
        val expectedValue = "gTazoqFvngVDSCkJGJQuokb"

        val encoded = Base58.encodeWithChecksum(value.toByteArray())

        assertThat(encoded).isEqualTo(expectedValue)
    }

    @Test
    fun decodeShouldReturnCorrectlyDecodedBytes() {
        val value = "72k1xXWG59wUsYv7h2"
        val expectedValue = "Hello, world!".toByteArray()

        val decoded = Base58.decode(value)

        assertThat(Arrays.equals(decoded, expectedValue)).isTrue()
    }

    @Test
    fun decodeWithChecksumShouldReturnCorrectlyDecodedBytes() {
        val value = "gTazoqFvngVDSCkJGJQuokb"
        val expectedValue = "Hello, world!".toByteArray()

        val decoded = Base58.decodeWithChecksum(value)

        assertThat(Arrays.equals(decoded, expectedValue)).isTrue()
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeWithChecksumShouldThrowExceptionWhenInvalidChecksum() {
        Base58.decodeWithChecksum("gTazoqFvngVDSCkJGJQuoka")
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeWithChecksumShouldThrowExceptionWhenInvalidInputLength() {
        Base58.decodeWithChecksum("gTa")
    }

}
