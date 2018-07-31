package io.openfuture.chain.crypto.domain

import io.openfuture.chain.crypto.component.key.ExtendedKeyDeserializer
import io.openfuture.chain.crypto.model.dto.ECKey
import io.openfuture.chain.crypto.util.HashUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ECKeyTest {

    @Test
    fun getAddressShouldReturnAddressWithMixedCaseCheckSum() {
        val address = createECKey(false).getAddress()

        assertThat(address).isEqualTo("0x5d446C3f03a61f2B4321443aEf1f66DFf97C1882")
    }

    @Test
    fun signShouldReturnSignature() {
        val data = HashUtils.sha256("Hello".toByteArray())
        val key = createECKey(true)

        val signature = key.sign(data)

        assertThat(signature).isNotEmpty()
    }

    @Test(expected = IllegalArgumentException::class)
    fun signShouldThrowIllegalArgumentExceptionWhenPrivateKeyIsEmpty() {
        val data = HashUtils.sha256("Hello".toByteArray())
        val key = createECKey(false)

        key.sign(data)
    }

    @Test
    fun verifyShouldReturnTrueWhenValidSignature() {
        val data = HashUtils.sha256("Hello".toByteArray())
        val key = createECKey(true)

        val signature = key.sign(data)
        val verify = key.verify(data, signature)

        assertThat(verify).isTrue()
    }

    @Test
    fun verifyShouldReturnFalseWhenInvalidSignature() {
        val data = HashUtils.sha256("Hello".toByteArray())
        val key = createECKey(true)

        val signature = key.sign(data)
        signature[signature.size - 1] = 0
        val verify = key.verify(data, signature)

        assertThat(verify).isFalse()
    }

    private fun createECKey(fromPrivate: Boolean): ECKey {
        val xpub = "xpub661MyMwAqRbcFtXgS5sYJABqqG9YLmC4Q1Rdap9gSE8NqtwybGhePY2gZ29ESFjqJoCu1Rupje8YtGqsefD265TMg7usUDFdp6W1EGMcet8"
        val xpriv = "xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHi"
        val serializedKey = if (fromPrivate) xpriv else xpub
        return ExtendedKeyDeserializer().deserialize(serializedKey).ecKey
    }
    
}