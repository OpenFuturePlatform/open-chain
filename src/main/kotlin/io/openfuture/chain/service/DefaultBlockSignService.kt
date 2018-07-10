package io.openfuture.chain.service

import io.openfuture.chain.block.BlockValidationService
import io.openfuture.chain.block.SignatureCollector
import io.openfuture.chain.block.SignaturePublicKeyPair
import io.openfuture.chain.crypto.key.KeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.Block
import io.openfuture.chain.nio.converter.BlockSignaturesConverter
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Service

@Service
class DefaultBlockSignService(
    private val blockValidationService: BlockValidationService,
    private val signatureManager: SignatureManager,
    private val keyHolder: KeyHolder,
    private val blockSignaturesConverter: BlockSignaturesConverter,
    private val signatureCollector: SignatureCollector
    // TODO here will be broadcast service
    // TODO signature checking service will be here
) : BlockSignService {

    override fun signBlock(block: Block): CommunicationProtocol.BlockSignatures {
        if (!blockValidationService.isValid(block)) {
            throw IllegalArgumentException("$block is not valid")
        }

        val signature = signatureManager.sign(HashUtils.hexStringToBytes(block.hash), keyHolder.getPrivateKey())

        val builder = CommunicationProtocol.BlockSignatures.newBuilder()
        blockSignaturesConverter.setBlockProto(builder, block)
        val signatures = listOf<CommunicationProtocol.SignaturePublicKeyPair>(
            CommunicationProtocol.SignaturePublicKeyPair.newBuilder()
                .setSignature(signature)
                .setPublicKey(HashUtils.bytesToHexString(keyHolder.getPublicKey()))
                .build()
        )
        return builder
            .addAllSignatures(signatures)
            .build()
    }

    override fun signBlock(blockSignatures: CommunicationProtocol.BlockSignatures) {
        val signatures = blockSignatures.signaturesList
            .map { SignaturePublicKeyPair(it.signature, it.publicKey) }.toHashSet()

        val block = blockSignaturesConverter.toBlock(blockSignatures)

        val signature = signatureManager.sign(HashUtils.hexStringToBytes(block.hash), keyHolder.getPrivateKey())
        val signaturePublicKeyPair = SignaturePublicKeyPair(
            signature,
            HashUtils.bytesToHexString(keyHolder.getPublicKey())
        )

        signatures.add(signaturePublicKeyPair)
    }

    override fun addSignatures(blockSignatures: CommunicationProtocol.BlockSignatures): Boolean {
        return signatureCollector.addBlockSignatures(blockSignatures)
    }

}