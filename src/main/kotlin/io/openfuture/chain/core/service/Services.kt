package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.core.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.core.model.dto.transaction.VoteTransactionDto
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import io.openfuture.chain.network.domain.NetworkMainBlock
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.node.*
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

    fun create(): NetworkMainBlock

    fun add(dto: NetworkMainBlock)

    fun isValid(block: NetworkMainBlock): Boolean

}

interface TransferTransactionService {

    fun getUnconfirmedByHash (hash: String): UTransferTransaction

    fun add(dto: TransferTransactionDto)

    fun add(request: TransferTransactionRequest)

    fun toBlock(hash: String, block: MainBlock)

}

interface VoteTransactionService {

    fun getAllUnconfirmed(): List<UVoteTransaction>

    fun getUnconfirmedByHash (hash: String): UVoteTransaction

    fun add(dto: VoteTransactionDto)

    fun add(request: VoteTransactionRequest)

    fun toBlock(hash: String, block: MainBlock)

}

interface DelegateTransactionService {

    fun getUnconfirmedByHash (hash: String): UDelegateTransaction

    fun add(dto: DelegateTransactionDto)

    fun add(request: DelegateTransactionRequest)

    fun toBlock(hash: String, block: MainBlock)

}

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

    fun increaseBalance(address: String, amount: Long)

    fun decreaseBalance(address: String, amount: Long)

    fun decreaseUnconfirmedBalance(address: String, amount: Long)

}

interface CommonBlockService {

    fun get(hash: String): BaseBlock

    fun getLast(): BaseBlock

    fun getBlocksAfterCurrentHash(hash: String): List<BaseBlock>?

    fun isExists(hash: String): Boolean

}