package io.openfuture.chain.component.initializer

import io.openfuture.chain.domain.block.BlockDto
import io.openfuture.chain.domain.block.nested.BlockData
import io.openfuture.chain.domain.block.nested.BlockHash
import io.openfuture.chain.domain.block.nested.MerkleHash
import io.openfuture.chain.nio.client.handler.ConnectionClientHandler
import io.openfuture.chain.service.DefaultBlockService
import io.openfuture.chain.util.BlockUtils
import io.openfuture.chain.crypto.util.HashUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class BlockchainInitializer(
        private val blockService: DefaultBlockService
) {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionClientHandler::class.java)
    }

    @PostConstruct
    fun initBlockChain() {
        if (0L == blockService.chainSize()) {
            blockService.add(createGenesisBlock())
        }

        log.info("Block chain successfully initialized")
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