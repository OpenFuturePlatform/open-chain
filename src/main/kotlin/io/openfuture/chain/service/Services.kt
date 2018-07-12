package io.openfuture.chain.service

import io.netty.channel.Channel
import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.crypto.RootAccountDto
import io.openfuture.chain.domain.hardware.*
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.network.domain.Peer
import io.openfuture.chain.protocol.CommunicationProtocol

interface HardwareInfoService {

    fun getHardwareInfo(): HardwareInfo

    fun getCpuInfo(): CpuInfo

    fun getRamInfo(): RamInfo

    fun getDiskStorageInfo(): List<StorageInfo>

    fun getNetworksInfo(): List<NetworkInfo>

}

interface BlockService {

    fun get(hash: String): Block

    fun getLast(): Block

    fun getLastMain(): MainBlock

    fun getLastGenesis(): GenesisBlock

    fun save(block: Block): Block

}

interface CryptoService {

    fun generateSeedPhrase(): String

    fun generateNewAccount(): RootAccountDto

    fun getRootAccount(seedPhrase: String): RootAccountDto

    fun getDerivationKey(seedPhrase: String, derivationPath: String): ExtendedKey

    fun importKey(key: String): ExtendedKey

    fun importWifKey(wifKey: String): ECKey

    fun serializePublicKey(key: ExtendedKey): String

    fun serializePrivateKey(key: ExtendedKey): String

}

interface ConsensusService {

    fun getCurrentEpochHeight(): Long

    fun isGenesisBlockNeeded(): Boolean

}

interface TransactionService {

    fun save(transaction: Transaction): Transaction

    fun saveAll(transactions: List<Transaction>): List<Transaction>

    fun getPendingTransactions(): List<Transaction>

}

interface WalletService {

    fun getBalance(address: String): Double

    fun updateByTransaction(transaction: Transaction)

}

interface NetworkService {

    fun broadcast(packet: CommunicationProtocol.Packet)

    fun maintainConnectionNumber()

    fun addPeer(channel: Channel, peer: Peer)

    fun removePeer(channel: Channel) : Peer?

    fun getPeers(): Set<Peer>

    fun connect(peers: List<CommunicationProtocol.Peer>)

}