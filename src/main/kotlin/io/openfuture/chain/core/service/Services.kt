package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.dto.transaction.BaseTransactionDto
import io.openfuture.chain.core.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.core.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.core.model.dto.transaction.VoteTransactionDto
import io.openfuture.chain.core.model.dto.transaction.data.BaseTransactionData
import io.openfuture.chain.core.model.dto.transaction.data.DelegateTransactionData
import io.openfuture.chain.core.model.dto.transaction.data.TransferTransactionData
import io.openfuture.chain.core.model.dto.transaction.data.VoteTransactionData
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
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

//interface TransactionService<Entity : Transaction, UEntity : UTransaction> {
//
//    fun toBlock(hash: String, block: MainBlock)
//
//}

interface TransferTransactionService {

    fun toBlock(dto: TransferTransactionDto, block: MainBlock)

}

interface VoteTransactionService {

    fun toBlock(dto: VoteTransactionDto, block: MainBlock)

}

interface DelegateTransactionService {

    fun add(dto: DelegateTransactionDto)

    fun add(request: DelegateTransactionRequest)

    fun toBlock(hash: String, block: MainBlock)

    fun toBlock(dto: DelegateTransactionDto, block: MainBlock)

}

interface UTransactionService<UEntity : UTransaction, Data : BaseTransactionData, Dto : BaseTransactionDto<Data>,
    Req : BaseTransactionRequest<UEntity, Data>> {

//    fun get(hash: String): UEntity

//    fun getAll(): MutableSet<UEntity>

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

interface WalletService {

    fun getByAddress(address: String): Wallet

    fun getUnspentBalanceByAddress(address: String): Long

    fun getBalanceByAddress(address: String): Long

    fun getVotesByAddress(address: String): MutableSet<Delegate>

    fun save(wallet: Wallet)

    fun updateBalanceByFee(address: String, fee: Long)

    fun updateUnconfirmedOut(address: String, fee: Long)

}

interface CommonBlockService {

    fun get(hash: String): BaseBlock

    fun getLast(): BaseBlock

    fun getBlocksAfterCurrentHash(hash: String): List<BaseBlock>?

    fun isExists(hash: String): Boolean

}