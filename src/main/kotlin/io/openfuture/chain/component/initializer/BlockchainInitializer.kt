package io.openfuture.chain.component.initializer

import io.openfuture.chain.domain.block.BlockDto
import io.openfuture.chain.domain.block.nested.BlockData
import io.openfuture.chain.domain.block.nested.BlockHash
import io.openfuture.chain.domain.block.nested.MerkleHash
import io.openfuture.chain.service.DefaultBlockService
import io.openfuture.chain.util.BlockUtils
import io.openfuture.chain.util.HashUtils
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class BlockchainInitializer(
        private val blockService: DefaultBlockService
) {

    @PostConstruct
    fun initBlockChain() {
        if (0L == blockService.chainSize()) {
            blockService.add(createGenesisBlock())
        }
    }

    // todo temp solution
    private fun createGenesisBlock(): BlockDto {
        val merkleHash = MerkleHash(HashUtils.generateHash("merkleHash".toByteArray()), listOf())
        val previousHash = HashUtils.generateHash("previousHash".toByteArray())
        val blockData = BlockData(0, 0, previousHash, merkleHash)
        val blockHash = BlockHash(0, BlockUtils.generateHash(blockData, 0))
        val signature = BlockUtils.generateSignature("genesisPrivateKey", blockData, blockHash)
        return BlockDto(blockData, blockHash, "genesisPublicKey", signature)
    }

}