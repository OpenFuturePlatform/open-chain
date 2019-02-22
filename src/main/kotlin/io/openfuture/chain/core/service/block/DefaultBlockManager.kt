package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.sync.SyncMode
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultBlockManager(
    private val repository: BlockRepository<Block>,
    private val genesisBlockService: GenesisBlockService,
    private val mainBlockService: MainBlockService,
    private val transactionManager: TransactionManager,
    private val stateManager: StateManager,
    private val receiptService: ReceiptService,
    private val properties: ConsensusProperties
) : BlockManager {


    override fun getCount(): Long = repository.count()

    override fun getLast(): Block = repository.findFirstByOrderByHeightDesc()

    override fun findByHash(hash: String): Block? = repository.findOneByHash(hash)

    override fun getAvgProductionTime(): Long {
        val lastBlock = getLast()
        val firstMainBlock = repository.findFirstByHeightGreaterThan(1) ?: return 0
        return (lastBlock.timestamp - firstMainBlock.timestamp) / lastBlock.height
    }

    override fun getAllByHeightIn(heights: List<Long>): List<Block> = repository.findAllByHeightIn(heights)

    override fun getGenesisBlockByHash(hash: String): GenesisBlock = genesisBlockService.getByHash(hash)

    override fun getMainBlockByHash(hash: String): MainBlock = mainBlockService.getByHash(hash)

    override fun getAllGenesisBlocks(request: PageRequest): Page<GenesisBlock> = genesisBlockService.getAll(request)

    override fun getAllMainBlocks(request: PageRequest): Page<MainBlock> = mainBlockService.getAll(request)

    override fun getPreviousGenesisBlock(hash: String): GenesisBlock = genesisBlockService.getPreviousBlock(hash)

    override fun getPreviousMainBlock(hash: String): MainBlock = mainBlockService.getPreviousBlock(hash)

    override fun getNextGenesisBlock(hash: String): GenesisBlock = genesisBlockService.getNextBlock(hash)

    override fun getNextMainBlock(hash: String): MainBlock = mainBlockService.getNextBlock(hash)

    override fun getPreviousGenesisBlockByHeight(height: Long): GenesisBlock =
        genesisBlockService.getPreviousByHeight(height)

    override fun getLastGenesisBlock(): GenesisBlock = genesisBlockService.getLast()

    override fun getLastMainBlock(): MainBlock = mainBlockService.getLast()

    override fun findGenesisBlockByEpochIndex(epochIndex: Long): GenesisBlock? =
        genesisBlockService.findByEpochIndex(epochIndex)

    override fun isGenesisBlockRequired(): Boolean = genesisBlockService.isGenesisBlockRequired()

    override fun getMainBlocksByEpochIndex(epochIndex: Long, syncMode: SyncMode): List<MainBlock> =
        mainBlockService.getBlocksByEpochIndex(epochIndex, syncMode)

    override fun createGenesisBlock(): GenesisBlock = genesisBlockService.create()

    override fun createMainBlock(): MainBlock = mainBlockService.create()

    @Transactional
    override fun add(block: Block) {
        when (block) {
            is GenesisBlock -> genesisBlockService.add(block)
            is MainBlock -> mainBlockService.add(block)
            else -> throw IllegalStateException("Wrong type")
        }
    }

    @Transactional
    override fun deleteByHeightIn(heights: List<Long>) {
        stateManager.deleteAll()
        transactionManager.deleteBlockTransactions(heights)
        receiptService.deleteBlockReceipts(heights)
        repository.deleteAllByHeightIn(heights)
    }

    @Transactional
    override fun removeEpoch(genesisBlock: GenesisBlock) {
        val fromHeight = if (1L == genesisBlock.height) {
            genesisBlock.height + 1
        } else {
            genesisBlock.height
        }
        val toHeight = fromHeight + properties.epochHeight!!
        val heightRange = (fromHeight..toHeight).toList()

        deleteByHeightIn(heightRange)
    }

}