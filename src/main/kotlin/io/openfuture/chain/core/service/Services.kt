package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.Contract
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.BlockPayload
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.state.State
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.*
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.model.node.*
import io.openfuture.chain.core.sync.SyncMode
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.BaseMainBlockMessage
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.data.domain.Page

interface HardwareInfoService {

    fun getHardwareInfo(): HardwareInfo

    fun getCpuInfo(): CpuInfo

    fun getRamInfo(): RamInfo

    fun getDiskStorageInfo(): List<StorageInfo>

    fun getNetworksInfo(): List<NetworkInfo>

}

/** Common block info service */
interface BlockService {

    fun getCount(): Long

    fun getLast(): Block

    fun save(block: Block)

    fun removeEpoch(genesisBlock: GenesisBlock)

    fun saveChunk(blocksChunk: List<Block>, syncMode: SyncMode)

    fun getAfterCurrentHash(hash: String): List<Block>

    fun isExists(hash: String): Boolean

    fun findByHash(hash: String): Block?

    fun getAvgProductionTime(): Long

    fun getCurrentHeight(): Long

    fun isExists(hash: String, height: Long): Boolean

    fun getAllByHeightIn(heights: List<Long>): List<Block>

    fun deleteByHeightIn(heights: List<Long>)

    fun isValidHash(block: Block): Boolean

    fun createHash(timestamp: Long, height: Long, previousHash: String, payload: BlockPayload): ByteArray
}

interface GenesisBlockService {

    fun getByHash(hash: String): GenesisBlock

    fun getAll(request: PageRequest): Page<GenesisBlock>

    fun getLast(): GenesisBlock

    fun findByEpochIndex(epochIndex: Long): GenesisBlock?

    fun create(): GenesisBlock

    fun add(block: GenesisBlock)

    fun getPreviousByHeight(height: Long): GenesisBlock

    fun getNextBlock(hash: String): GenesisBlock

    fun getPreviousBlock(hash: String): GenesisBlock

    fun isGenesisBlockRequired(): Boolean

    fun add(message: GenesisBlockMessage)

}

interface MainBlockService {

    fun getByHash(hash: String): MainBlock

    fun getAll(request: PageRequest): Page<MainBlock>

    fun create(): PendingBlockMessage

    fun add(message: BaseMainBlockMessage)

    fun verify(message: PendingBlockMessage): Boolean

    fun getPreviousBlock(hash: String): MainBlock

    fun getNextBlock(hash: String): MainBlock

    fun getBlocksByEpochIndex(epochIndex: Long): List<MainBlock>

}

/** Base transaction service */
interface BaseTransactionService {

    fun getCount(): Long

    fun getUnconfirmedBalanceBySenderAddress(address: String): Long

    fun getProducingPerSecond(): Long

    fun deleteBlockTransactions(blockHeights: List<Long>)

}

interface UTransactionService<uT : UnconfirmedTransaction> {

    fun findByHash(hash: String): uT?

    fun getAll(): List<uT>

    fun getAll(request: PageRequest): List<uT>

    fun getAllBySenderAddress(address: String): List<uT>

    fun save(uTx: uT): uT

    fun remove(uTx: uT)

}

interface UDelegateTransactionService : UTransactionService<UnconfirmedDelegateTransaction>

interface UTransferTransactionService : UTransactionService<UnconfirmedTransferTransaction>

interface UVoteTransactionService : UTransactionService<UnconfirmedVoteTransaction> {

    fun getUnconfirmedBySenderAgainstDelegate(senderAddress: String, delegateKey: String): UnconfirmedVoteTransaction?

}

interface TransactionService<T : Transaction> {

    fun getByHash(hash: String): T

    fun getAll(request: PageRequest): Page<T>

}

interface RewardTransactionService : TransactionService<RewardTransaction> {

    fun getByBlock(block: Block): RewardTransaction

    fun getByRecipientAddress(address: String): List<RewardTransaction>

    fun create(timestamp: Long, fees: Long): RewardTransaction

    fun commit(tx: RewardTransaction): RewardTransaction

    fun process(tx: RewardTransaction): Receipt

}

interface ExternalTransactionService<T : Transaction, uT : UnconfirmedTransaction> : TransactionService<T> {

    fun getAllByBlock(block: Block): List<T>

    fun add(uTx: uT): uT

    fun commit(tx: T, receipt: Receipt): T

    fun process(uTx: uT, delegateWallet: String): Receipt

}

interface TransferTransactionService : ExternalTransactionService<TransferTransaction, UnconfirmedTransferTransaction> {

    fun getByAddress(address: String, request: PageRequest): Page<TransferTransaction>

}

interface VoteTransactionService : ExternalTransactionService<VoteTransaction, UnconfirmedVoteTransaction> {

    fun getLastVoteForDelegate(senderAddress: String, delegateKey: String): VoteTransaction

}

interface DelegateTransactionService : ExternalTransactionService<DelegateTransaction, UnconfirmedDelegateTransaction>

interface TransactionValidatorManager {

    fun validateNew(utx: UnconfirmedTransaction)

    fun verify(tx: BaseTransaction): Boolean

}

interface TransactionValidator<T> {

    fun validateNew(utx: T)

    fun validate(utx: T)

}

interface DelegateTransactionValidator : TransactionValidator<UnconfirmedDelegateTransaction>

interface TransferTransactionValidator : TransactionValidator<UnconfirmedTransferTransaction>

interface VoteTransactionValidator : TransactionValidator<UnconfirmedVoteTransaction>

interface StateManager {

    fun <T : State> getLastByAddress(address: String): T

    fun getAllByBlock(block: Block): List<State>

    fun getWalletBalanceByAddress(address: String): Long

    fun getVotesForDelegate(delegateKey: String): List<AccountState>

    fun updateWalletBalanceByAddress(address: String, amount: Long)

    fun updateVoteByAddress(address: String, delegateKey: String, voteType: VoteType)

    fun updateSmartContractStorage(address: String, storage: String)

    fun getAllDelegates(request: PageRequest): List<DelegateState>

    fun getActiveDelegates(): List<DelegateState>

    fun isExistsDelegateByPublicKey(key: String): Boolean

    fun isExistsDelegatesByPublicKeys(publicKeys: List<String>): Boolean

    fun addDelegate(delegateKey: String, walletAddress: String, createDate: Long)

    fun updateDelegateRating(delegateKey: String, amount: Long)

    fun commit(state: State)

    fun deleteBlockStates(blockHeights: List<Long>)

}

interface DelegateStateService {

    fun getAllDelegates(request: PageRequest): List<DelegateState>

    fun getActiveDelegates(): List<DelegateState>

    fun isExistsByPublicKey(key: String): Boolean

    fun isExistsByPublicKeys(publicKeys: List<String>): Boolean

    fun addDelegate(delegateKey: String, walletAddress: String, createDate: Long): DelegateState

    fun updateRating(delegateKey: String, amount: Long): DelegateState

}

interface AccountStateService {

    fun getBalanceByAddress(address: String): Long

    fun getVotesForDelegate(delegateKey: String): List<AccountState>

    fun updateBalanceByAddress(address: String, amount: Long): AccountState

    fun updateVoteByAddress(address: String, delegateKey: String?): AccountState

    fun updateStorage(address: String, storage: String): AccountState

}

interface ContractService {

    fun getByAddress(address: String): Contract

    fun getAllByOwner(owner: String): List<Contract>

    fun save(contract: Contract): Contract

    fun generateAddress(owner: String): String
}

interface ReceiptService {

    fun getByTransactionHash(hash: String): Receipt

    fun commit(receipt: Receipt)

    fun deleteBlockReceipts(blockHeights: List<Long>)

}