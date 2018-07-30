package io.openfuture.chain.crypto.domain

import io.openfuture.chain.crypto.component.key.ExtendedKeySerializer
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Test

class ExtendedKeyTest {

    @Test
    fun rootExtendedKeyShouldBeGeneratedAccordingToBip32Specification() {
        val serializer = ExtendedKeySerializer()
        val seed = ByteUtils.fromHexString("000102030405060708090a0b0c0d0e0f")
        val expectedPrivate = "xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHi"
        val expectedPublic = "xpub661MyMwAqRbcFtXgS5sYJABqqG9YLmC4Q1Rdap9gSE8NqtwybGhePY2gZ29ESFjqJoCu1Rupje8YtGqsefD265TMg7usUDFdp6W1EGMcet8"

        val key = ExtendedKey.root(seed)
        val serializedPrivateKey = serializer.serializePrivate(key)
        val serializedPublicKey = serializer.serializePublic(key)

        assertThat(serializedPrivateKey).isEqualTo(expectedPrivate)
        assertThat(serializedPublicKey).isEqualTo(expectedPublic)
    }

}