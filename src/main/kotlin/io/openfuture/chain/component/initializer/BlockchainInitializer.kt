package io.openfuture.chain.component.initializer

import io.openfuture.chain.domain.block.MinedBlockDto
import io.openfuture.chain.domain.block.nested.BlockHash
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.util.HashUtils
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class BlockchainInitializer(
        private val blockService: BlockService
) {

    @PostConstruct
    fun initBlockChain() {
        if (isEmptyBlockChain()) {
            initGenesisBlock()
        }
//        updateBlockChainFormNetwork() //todo get blocks and pending transactions from network
    }

    private fun initGenesisBlock() {
        blockService.save(createGenesisBlock())
    }

    private fun isEmptyBlockChain(): Boolean {
        return 0L == blockService.count()
    }

    // todo temp, need discuss it
    private fun createGenesisBlock(): MinedBlockDto {
        return MinedBlockDto(0, 1, generateGenesisPreviousHash(), generateGenesisTransactions(),
                generateGenesisMerkleHash(), generateGenesisBlockHash(), generateGenesisPublicKey(),
                generateGenesisSignature())
    }

    private fun generateGenesisMerkleHash(): String {
        return HashUtils.generateHash("merkleHash".toByteArray())
    }

    private fun generateGenesisPreviousHash(): String {
        return HashUtils.generateHash("previousHash".toByteArray())
    }

    private fun generateGenesisPublicKey(): String {
        return HashUtils.generateHash("publicKey".toByteArray())
    }

    private fun generateGenesisSignature(): String {
        return HashUtils.generateHash("signature".toByteArray())
    }

    private fun generateGenesisBlockHash(): BlockHash {
        val hash = HashUtils.generateHash("genesisHash".toByteArray())
        return BlockHash(0, hash)
    }

    private fun generateGenesisTransactions(): MutableList<TransactionDto> {
        return mutableListOf()
    }

}