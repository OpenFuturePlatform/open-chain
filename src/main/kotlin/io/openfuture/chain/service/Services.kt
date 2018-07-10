package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.crypto.key.WalletDto
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StorageInfo
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.protocol.CommunicationProtocol
import java.nio.channels.Channel

interface HardwareInfoService {

    fun getHardwareInfo(): HardwareInfo

    fun getCpuInfo(): CpuInfo

    fun getRamInfo(): RamInfo

    fun getDiskStorageInfo(): List<StorageInfo>

    fun getNetworksInfo(): List<NetworkInfo>

}

interface BlockService {

    fun get(id: Int): Block

    fun getAll(): MutableList<Block>

    fun getLast(): Block?

}

interface CryptoService {

    fun generateSeedPhrase(): String

    fun getMasterKey(seedPhrase: String): ExtendedKey

    fun getDerivationKey(seedPhrase: String, derivationPath: String): ExtendedKey

    fun importKey(key: String): ExtendedKey

    fun importWifKey(wifKey: String): ECKey

    fun serializePublicKey(key: ExtendedKey): String

    fun serializePrivateKey(key: ExtendedKey): String

    fun generateKey(): WalletDto

}

interface TransactionService {

    fun save(transaction: Transaction): Transaction

    fun getPendingTransactions(): List<Transaction>

}

interface NetworkService {

    fun joinNetwork(host : String, port: Int)

    fun handleJoinResponse(message: CommunicationProtocol.Packet, channel: Channel)

    fun connect(host : String, port: Int)

    fun broadcast(packet: CommunicationProtocol.Packet)

}