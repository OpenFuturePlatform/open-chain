package io.openfuture.chain.util

import io.openfuture.chain.crypto.util.Base58CoderUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class Base58CoderUtilsTest {

    @Test
    fun encodeShouldReturnCorrectlyEncodedBytes() {
        val value = "Hello, world!"
        val expectedValue = "72k1xXWG59wUsYv7h2"

        val encoded = Base58CoderUtils.encode(value.toByteArray())

        assertThat(encoded).isEqualTo(expectedValue)
    }

    @Test
    fun encodeShouldReturnEmptyStringWhenBytesToEncodeAreEmpty() {
        val encoded = Base58CoderUtils.encode(ByteArray(0))

        assertThat(encoded).isEmpty()
    }

    @Test
    fun encodeWithChecksumShouldReturnCorrectlyEncodedBytes() {
        val value = "Hello, world!"
        val expectedValue = "gTazoqFvngVDSCkJGJQuokb"

        val encoded = Base58CoderUtils.encodeWithChecksum(value.toByteArray())

        assertThat(encoded).isEqualTo(expectedValue)
    }

    @Test
    fun decodeShouldReturnCorrectlyDecodedBytes() {
        val value = "72k1xXWG59wUsYv7h2"
        val expectedValue = "Hello, world!".toByteArray()

        val decoded = Base58CoderUtils.decode(value)

        assertThat(Arrays.equals(decoded, expectedValue)).isTrue()
    }

    @Test
    fun decodeWithChecksumShouldReturnCorrectlyDecodedBytes() {
        val value = "gTazoqFvngVDSCkJGJQuokb"
        val expectedValue = "Hello, world!".toByteArray()

        val decoded = Base58CoderUtils.decodeWithChecksum(value)

        assertThat(Arrays.equals(decoded, expectedValue)).isTrue()
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeWithChecksumShouldThrowExceptionWhenInvalidChecksum() {
        Base58CoderUtils.decodeWithChecksum("gTazoqFvngVDSCkJGJQuoka")
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeWithChecksumShouldThrowExceptionWhenInvalidInputLength() {
        Base58CoderUtils.decodeWithChecksum("gTa")
    }

}
