package io.openfuture.chain.util

import org.junit.Assert
import org.junit.Test
import java.util.*

class Base58CoderUtilsTest {

    @Test
    fun encodeShouldCorrectlyEncodeBytes() {
        val value = "Hello, world!"
        val expectedValue = "72k1xXWG59wUsYv7h2"

        val encoded = Base58CoderUtils.encode(value.toByteArray())

        Assert.assertTrue(encoded == expectedValue)
    }

    @Test
    fun encodeShouldReturnEmptyStringWhenEmptyBytes() {
        val encoded = Base58CoderUtils.encode(ByteArray(0))

        Assert.assertTrue(encoded.isEmpty())
    }

    @Test
    fun encodeWithChecksumShouldCorrectlyEncodeBytes() {
        val value = "Hello, world!"
        val expectedValue = "gTazoqFvngVDSCkJGJQuokb"

        val encoded = Base58CoderUtils.encodeWithChecksum(value.toByteArray())

        Assert.assertTrue(encoded == expectedValue)
    }

    @Test
    fun decodeShouldCorrectlyDecodeBytes() {
        val value = "72k1xXWG59wUsYv7h2"
        val expectedValue = "Hello, world!".toByteArray()

        val decoded = Base58CoderUtils.decode(value)

        Assert.assertTrue(Arrays.equals(decoded, expectedValue))
    }

    @Test
    fun decodeWithChecksumShouldCorrectlyDecodeBytes() {
        val value = "gTazoqFvngVDSCkJGJQuokb"
        val expectedValue = "Hello, world!".toByteArray()

        val decoded = Base58CoderUtils.decodeWithChecksum(value)

        Assert.assertTrue(Arrays.equals(decoded, expectedValue))
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
