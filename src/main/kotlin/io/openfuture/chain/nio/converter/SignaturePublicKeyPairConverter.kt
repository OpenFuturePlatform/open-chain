package io.openfuture.chain.nio.converter

import io.openfuture.chain.domain.block.SignaturePublicKeyPair
import io.openfuture.chain.protocol.CommunicationProtocol

class SignaturePublicKeyPairConverter
    : MessageConverter<SignaturePublicKeyPair, CommunicationProtocol.SignaturePublicKeyPair> {

    override fun fromEntity(entity: SignaturePublicKeyPair): CommunicationProtocol.SignaturePublicKeyPair {
        return CommunicationProtocol.SignaturePublicKeyPair.newBuilder()
            .setSignature(entity.signature)
            .setPublicKey(entity.publicKey)
            .build()
    }

    override fun fromMessage(message: CommunicationProtocol.SignaturePublicKeyPair): SignaturePublicKeyPair {
        return SignaturePublicKeyPair(message.signature, message.publicKey)
    }

}