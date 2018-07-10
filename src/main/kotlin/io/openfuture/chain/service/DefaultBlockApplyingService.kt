package io.openfuture.chain.service

import io.openfuture.chain.block.BlockValidationService
import io.openfuture.chain.crypto.key.KeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.Block
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.repository.BlockRepository
import org.springframework.stereotype.Service

@Service
class DefaultBlockApplyingService(
    val blockValidationService: BlockValidationService,
    val blockRepository: BlockRepository,
    val signatureManager: SignatureManager,
    val keyHolder: KeyHolder
    // TODO here will be broadcast service
    // TODO signature checking service will be here
) : BlockApplyingService {

    override fun sendBlockToSign(block: Block) {
        // TODO we will send block to another nodes using broadcast method
    }

    override fun signBlock(block: Block): CommunicationProtocol.BlockSignature {
        if (!blockValidationService.isValid(block)) {
            throw IllegalArgumentException("$block is not valid")
        }

        val signature = signatureManager.sign(HashUtils.hexStringToBytes(block.hash), keyHolder.getPrivateKey())
        return CommunicationProtocol.BlockSignature.newBuilder()
            .setBlockHash(block.hash)
            .setPublicKey(HashUtils.bytesToHexString(keyHolder.getPublicKey()))
            .setSignature(signature)
            .build()
    }

    override fun sendSignedBlockOrBroadcast(blockSignature: CommunicationProtocol.BlockSignature) {
        // TODO we will send signed block back if it's active delegate or broadcast from handler else
    }

    override fun sendFullSignedBlock(fullSignedBlock: CommunicationProtocol.FullSignedBlock) {
        // TODO after signed blocks will be collected
        // TODO we will send signed with all delegates block to each active delegates after mergeBlockSigns method from
        // TODO handler if it's signed by more than 50%
    }

    override fun applyBlock(fullSignedBlock: CommunicationProtocol.FullSignedBlock) {
        val block = fullSignedBlock.block
        val signatures = fullSignedBlock.signatures

        if (!blockValidationService.isValid(block)) {
            throw IllegalArgumentException("$block is not valid")
        }

        for (signature in signatures) {
            // TODO here we will check delegates signatures, if signature is wrong we'll throw an exception that
            // TODO signature is wrong
        }

        blockRepository.save(block)
    }

}