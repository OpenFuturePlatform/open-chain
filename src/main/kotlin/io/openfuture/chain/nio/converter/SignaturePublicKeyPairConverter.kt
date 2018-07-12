package io.openfuture.chain.nio.converter

import io.openfuture.chain.domain.block.SignaturePublicKeyPair
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component

@Component
class SignaturePublicKeyPairConverter
    : MessageConverter<SignaturePublicKeyPair, CommunicationProtocol.SignaturePublicKeyPair> {

    private val signaturePublicKeyPairBuilder = CommunicationProtocol.SignaturePublicKeyPair.newBuilder()


    override fun fromEntity(entity: SignaturePublicKeyPair): CommunicationProtocol.SignaturePublicKeyPair {
        return signaturePublicKeyPairBuilder
            .setSignature(entity.signature)
            .setPublicKey(entity.publicKey)
            .build()
    }

    override fun fromMessage(message: CommunicationProtocol.SignaturePublicKeyPair): SignaturePublicKeyPair {
        return SignaturePublicKeyPair(message.signature, message.publicKey)
    }

}