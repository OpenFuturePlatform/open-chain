package io.openfuture.chain.crypto.component

import io.openfuture.chain.crypto.component.key.DerivationKeysHelper
import io.openfuture.chain.crypto.component.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.domain.ExtendedKey
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Test

class DerivationKeysHelperTest {

    private val helper = DerivationKeysHelper()


    @Test
    fun deriveShouldGenerateKeyPairAccordingToBip32Specification() {
        val serializer = ExtendedKeySerializer()
        val seed = ByteUtils
            .fromHexString("fffcf9f6f3f0edeae7e4e1dedbd8d5d2cfccc9c6c3c0bdbab7b4b1aeaba8a5a29f9c999693908d8a8784817e7b7875726f6c696663605d5a5754514e4b484542")

        val rootKey = ExtendedKey.root(seed)
        val serializedPrivateKeyRoot = serializer.serializePrivate(rootKey)
        val serializedPublicKeyRoot = serializer.serializePublic(rootKey)

        assertThat(serializedPrivateKeyRoot)
            .isEqualTo("xprv9s21ZrQH143K31xYSDQpPDxsXRTUcvj2iNHm5NUtrGiGG5e2DtALGdso3pGz6ssrdK4PFmM8NSpSBHNqPqm55Qn3LqFtT2emdEXVYsCzC2U")
        assertThat(serializedPublicKeyRoot)
            .isEqualTo("xpub661MyMwAqRbcFW31YEwpkMuc5THy2PSt5bDMsktWQcFF8syAmRUapSCGu8ED9W6oDMSgv6Zz8idoc4a6mr8BDzTJY47LJhkJ8UB7WEGuduB")

        val derivedKey = helper.derive(rootKey, "m/0")
        val serializedPrivateKey = serializer.serializePrivate(derivedKey)
        val serializedPublicKey = serializer.serializePublic(derivedKey)

        assertThat(serializedPrivateKey)
            .isEqualTo("xprv9vHkqa6EV4sPZHYqZznhT2NPtPCjKuDKGY38FBWLvgaDx45zo9WQRUT3dKYnjwih2yJD9mkrocEZXo1ex8G81dwSM1fwqWpWkeS3v86pgKt")
        assertThat(serializedPublicKey)
            .isEqualTo("xpub69H7F5d8KSRgmmdJg2KhpAK8SR3DjMwAdkxj3ZuxV27CprR9LgpeyGmXUbC6wb7ERfvrnKZjXoUmmDznezpbZb7ap6r1D3tgFxHmwMkQTPH")
    }

}