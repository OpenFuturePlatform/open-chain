package io.openfuture.chain.crypto.component.key

import io.openfuture.chain.crypto.model.dto.ExtendedKey
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Test

class DerivationKeysHelperTest {

    private val helper = DerivationKeysHelper()


    @Test
    fun deriveShouldGenerateKeyPairAccordingToBip32Specification() {
        val serializer = ExtendedKeySerializer()
        val seed = ByteUtils.fromHexString("000102030405060708090a0b0c0d0e0f")

        val rootKey = ExtendedKey.root(seed)
        val serializedPrivateKeyRoot = serializer.serializePrivate(rootKey)
        val serializedPublicKeyRoot = serializer.serializePublic(rootKey)

        assertThat(serializedPrivateKeyRoot)
            .isEqualTo("xprv9s21ZrQH143K3e56yqTX9wsqYLqfHnc2j1hR846DGgk1g4jzVUEgoq37n5Hp45CLSr5hNZaYYeMZSCVFpdVmGJYEYiL13QMzoy4igY7zKCE")
        assertThat(serializedPublicKeyRoot)
            .isEqualTo("xpub661MyMwAqRbcG89a5rzXX5pa6Ng9hFKt6Ed1vSVpq2GzYs5931YwMdMbdMREX9TSZupu8DPotD3JF6EXFGpsUYGbN2saY3ZzE76FHBS7ajD")

        val derivedKey = helper.derive(rootKey, "m/0")
        val serializedPrivateKey = serializer.serializePrivate(derivedKey)
        val serializedPublicKey = serializer.serializePublic(derivedKey)

        assertThat(serializedPrivateKey)
            .isEqualTo("xprv9vLHhghHJPp2Rn3g1Xq2pkaQ49chnWyMmrZBy4wqb3yUtR8MkHnKve2jipp7tGnWp4EWvfdturgWeCqrc4nfXhPcS3b9cm6MBu7DnJtFLU1")
        assertThat(serializedPublicKey)
            .isEqualTo("xpub69Ke7CEB8mNKeG897ZN3BtX8cBTCByhD95UnmTMT9PWTmDTWHq6aUSMDa94bwSMS3bwaiWQTjwvypWHewLWu3dX29yEChWz4pnTrkWJrP5f")
    }

}