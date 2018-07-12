package io.openfuture.chain.nio.converter

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.domain.block.SignaturePublicKeyPair
import io.openfuture.chain.protocol.CommunicationProtocol
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test

class SignaturePublicKeyPairConverterTests : ServiceTests() {

    private lateinit var signaturePublicKeyPairConverter: SignaturePublicKeyPairConverter


    companion object {
        fun createSignaturePublicKeyPair(
            signature: String,
            publicKey: String
        ): CommunicationProtocol.SignaturePublicKeyPair {
            return CommunicationProtocol.SignaturePublicKeyPair.newBuilder()
                .setSignature(signature)
                .setPublicKey(publicKey)
                .build()
        }
    }

    @Before
    fun setUp() {
        signaturePublicKeyPairConverter = SignaturePublicKeyPairConverter()
    }

    @Test
    fun fromEntityShouldReturnSignaturePublicKeyPairMessage() {
        val signature = "signature"
        val publicKey = "publicKey"

        val signaturePublicKeyPair = SignaturePublicKeyPair(signature, publicKey)

        val signatureMessage = signaturePublicKeyPairConverter.fromEntity(signaturePublicKeyPair)

        Assertions.assertThat(signatureMessage.signature).isEqualTo(signature)
        Assertions.assertThat(signatureMessage.publicKey).isEqualTo(publicKey)
    }

    @Test
    fun fromMessage() {
        val signature = "signature"
        val publicKey = "publicKey"

        val signatureMessage = createSignaturePublicKeyPair(signature, publicKey)

        val signaturePublicKeyPair = signaturePublicKeyPairConverter.fromMessage(signatureMessage)

        Assertions.assertThat(signaturePublicKeyPair.signature).isEqualTo(signature)
        Assertions.assertThat(signaturePublicKeyPair.publicKey).isEqualTo(publicKey)
    }

}