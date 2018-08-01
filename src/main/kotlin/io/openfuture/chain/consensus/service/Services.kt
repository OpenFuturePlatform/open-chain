package io.openfuture.chain.consensus.service

import io.openfuture.chain.consensus.model.dto.transaction.BaseTransactionDto
import io.openfuture.chain.consensus.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.consensus.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.consensus.model.dto.transaction.VoteTransactionDto
import io.openfuture.chain.consensus.model.dto.transaction.data.BaseTransactionData
import io.openfuture.chain.consensus.model.dto.transaction.data.DelegateTransactionData
import io.openfuture.chain.consensus.model.dto.transaction.data.TransferTransactionData
import io.openfuture.chain.consensus.model.dto.transaction.data.VoteTransactionData
import io.openfuture.chain.consensus.model.entity.Delegate
import io.openfuture.chain.consensus.model.entity.Wallet
import io.openfuture.chain.consensus.model.entity.block.GenesisBlock
import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.consensus.model.entity.transaction.DelegateTransaction
import io.openfuture.chain.consensus.model.entity.transaction.TransferTransaction
import io.openfuture.chain.consensus.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.consensus.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.consensus.model.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.core.model.entity.transaction.UTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import io.openfuture.chain.network.domain.NetworkMainBlock
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.node.*
import io.openfuture.chain.rpc.domain.transaction.BaseTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.TransferTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.VoteTransactionRequest
import org.springframework.data.domain.Page

interface HardwareInfoService {

    fun getHardwareInfo(): HardwareInfo

    fun getCpuInfo(): CpuInfo

    fun getRamInfo(): RamInfo

    fun getDiskStorageInfo(): List<StorageInfo>

    fun getNetworksInfo(): List<NetworkInfo>

}

interface GenesisBlockService {

    fun add(dto: NetworkGenesisBlock)

    fun getLast(): GenesisBlock

    fun save(block: GenesisBlock): GenesisBlock

    fun isValid(block: GenesisBlock): Boolean

}

interface MainBlockService {

    fun add(dto: NetworkMainBlock)

    fun getLast(): MainBlock

    fun save(block: MainBlock): MainBlock

    fun isValid(block: MainBlock): Boolean

}

interface TransactionService<Entity : Transaction, UEntity : UTransaction> {

    fun toBlock(hash: String, block: MainBlock)

}

interface TransferTransactionService : TransactionService<TransferTransaction, UTransferTransaction> {

    fun toBlock(dto: TransferTransactionDto, block: MainBlock)

}

interface VoteTransactionService : TransactionService<VoteTransaction, UVoteTransaction> {

    fun toBlock(dto: VoteTransactionDto, block: MainBlock)

}

interface DelegateTransactionService : TransactionService<DelegateTransaction, UDelegateTransaction> {

    fun toBlock(dto: DelegateTransactionDto, block: MainBlock)

}

interface UTransactionService<UEntity : UTransaction, Data : BaseTransactionData, Dto : BaseTransactionDto<Data>,
    Req : BaseTransactionRequest<UEntity, Data>> {

    fun get(hash: String): UEntity

    fun getAll(): MutableSet<UEntity>

    fun add(dto: Dto): UEntity

    fun add(request: Req): UEntity

}

interface UTransferTransactionService : UTransactionService<UTransferTransaction, TransferTransactionData,
    TransferTransactionDto, TransferTransactionRequest>

interface UVoteTransactionService : UTransactionService<UVoteTransaction, VoteTransactionData,
    VoteTransactionDto, VoteTransactionRequest>

interface UDelegateTransactionService : UTransactionService<UDelegateTransaction, DelegateTransactionData,
    DelegateTransactionDto, DelegateTransactionRequest>

interface DelegateService {

    fun getAll(request: PageRequest): Page<Delegate>

    fun getByPublicKey(key: String): Delegate

    fun getActiveDelegates(): Set<Delegate>

    fun save(delegate: Delegate): Delegate

}

interface ConsensusService {

    fun getCurrentEpochHeight(): Long

    fun isGenesisBlockNeeded(): Boolean

}

interface WalletService {

    fun getByAddress(address: String): Wallet

    fun getBalanceByAddress(address: String): Long

    fun getVotesByAddress(address: String): MutableSet<Delegate>

    fun save(wallet: Wallet)

    fun updateBalance(from: String, to: String, amount: Long, fee: Long)

}