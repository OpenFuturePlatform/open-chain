package io.openfuture.chain.crypto.domain

import io.openfuture.chain.crypto.component.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.model.dto.ExtendedKey
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Test

class ExtendedKeyTest {

    @Test
    fun rootExtendedKeyShouldBeGeneratedAccordingToBip32Specification() {
        val serializer = ExtendedKeySerializer()
        val seed = ByteUtils.fromHexString("000102030405060708090a0b0c0d0e0f")
        val expectedPrivate = "xprv9s21ZrQH143K3e56yqTX9wsqYLqfHnc2j1hR846DGgk1g4jzVUEgoq37n5Hp45CLSr5hNZaYYeMZSCVFpdVmGJYEYiL13QMzoy4igY7zKCE"
        val expectedPublic = "xpub661MyMwAqRbcG89a5rzXX5pa6Ng9hFKt6Ed1vSVpq2GzYs5931YwMdMbdMREX9TSZupu8DPotD3JF6EXFGpsUYGbN2saY3ZzE76FHBS7ajD"

        val key = ExtendedKey.root(seed)
        val serializedPrivateKey = serializer.serializePrivate(key)
        val serializedPublicKey = serializer.serializePublic(key)

        assertThat(serializedPrivateKey).isEqualTo(expectedPrivate)
        assertThat(serializedPublicKey).isEqualTo(expectedPublic)
    }

}