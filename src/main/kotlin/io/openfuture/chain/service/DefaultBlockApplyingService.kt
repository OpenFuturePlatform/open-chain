package io.openfuture.chain.service

import io.openfuture.chain.block.BlockValidationService
import io.openfuture.chain.block.message.FullSignedBlock
import io.openfuture.chain.block.message.SignedBlock
import io.openfuture.chain.entity.Block
import io.openfuture.chain.repository.BlockRepository
import org.springframework.stereotype.Service

@Service
class DefaultBlockApplyingService(
    val blockValidationService: BlockValidationService,
    val blockRepository: BlockRepository
    // TODO here will be broadcast service
    // TODO signature checking service will be here
) : BlockApplyingService {

    override fun sendBlockToSign(block: Block) {
        // TODO we will send block to another nodes using broadcast method
    }

    override fun signBlock(block: Block): SignedBlock {
        val signature = ""//TODO we will sign block with sing service that we have no from handler
        return SignedBlock(block, signature)
    }

    override fun sendSignedBlock(signedBlock: SignedBlock) {
        // TODO we will send signed block back if it's active delegate or broadcast from handler else
    }

    override fun sendFullSignedBlock(fullSignedBlock: FullSignedBlock) {
        // TODO we will send signed with all delegates block to each active delegates after mergeBlockSigns method from
        // handler if it's signed by more than 50%
    }

    override fun applyBlock(fullSignedBlock: FullSignedBlock) {
        val block = fullSignedBlock.block
        val signatures = fullSignedBlock.signatures

        if (!blockValidationService.isValid(block)) {
            throw IllegalArgumentException("$block is not valid")
        }

        for (signature in signatures) {
            // TODO here we will check delegates signatures, if signature is wrong we'll throw an exception that
            // signature is wrong
        }

        blockRepository.save(block)
    }

}