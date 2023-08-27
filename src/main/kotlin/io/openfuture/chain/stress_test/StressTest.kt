package io.openfuture.chain.stress_test

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.concurrent.CountDownLatch
import kotlin.experimental.and


class StressTest(private val transactionManager: TransactionManager) {

    private val concurrentRequests = 100
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val latch = CountDownLatch(concurrentRequests)

    fun runLoadTest() {
        for (i in 1..concurrentRequests) {
            coroutineScope.launch {
                val request = createRandomTransferRequest()
                val response = transactionManager.add(UnconfirmedTransferTransaction.of(request))
                println("Request $i completed with response hash: ${response.hash}")
                latch.countDown()
            }
        }
        latch.await()
    }

    private fun createRandomTransferRequest(): TransferTransactionRequest {
        val recipientAddress = recipientAddresses.random()
        val randomHash = generateRandomHexHash(64) // Generate a hash of length 64
        val randomHexHash = generateRandomHexHash(10)

        return TransferTransactionRequest(
            timestamp = System.currentTimeMillis(),
            fee = 1,
            hash = randomHash,
            senderAddress = "0x6f7c626D720044905536009AD0c637625e5F57F5",
            senderSignature = "MEUCIHSTpgOW+b6QB1eX7X56gNhl75xcpDhpV60XxFzYNwNkAiEA1sjoQg94fP+847cjC1/PrZsDv+eBp4ygOw$randomHexHash",
            senderPublicKey = "03609ceae592ac63503be363c99570da723bcb1e637c35c5a9044c78d826a3f79e",
            amount = 2,
            recipientAddress = recipientAddress,
            data = null
        )
    }
}

private val recipientAddresses = listOf(
    "0x523a4a63d1d3Aa7CEdbb7FB9d1363DE76DdBC2FD",
    "0xE484275A491551de9944FAE8e9168bF5afA1Eb1e",
    "0x523a4a63d1d3Aa7CEdbb7FB9d1363DE76DdBC2FD",
    "0xE484275A491551de9944FAE8e9168bF5afA1Eb1e",
    "0x49661d192C5Dd3C6097BF2edec5905cc6d691649",
    "0x56e8DFcF58Af2922faf05fecd8F9c156B93C920f",
    "0x8ADD8540CB4f474E4863455f7C7D2d876Ac84475",
    "0xb1C95326174C06cafCa87140931918D522C501a7",
    "0x4e93E5D112e878672888F5aE794287B28846f252",
    "0x0A39e3C3dF795bd4Cd057A2fE381D2053eF84D7C",
    "0xacD267a983c1A415DB0B96c62D443A9ba6C75B7E",
    "0xe307b01801Ff02062aB3db937Ca0dAb836eC3EE0",
    "0xb10A654bE8bb9144481Cb072053f5C11a81bC06e",
    "0x55A763C173D3B77E2d00e8ED2Bb910E8c4a4308f",
    "0xBD23fec0B69300bb18e2a82416A05ecFc9256D74",
    "0x54466Ae803910baE12281bE814039F6457f95c62",
    "0x875CEe49e89E963003E7C02a949Ca1BA0b0dA8b5",
    "0x744e5f8Efb4dbfEC8b96F6429F9ce86904735736",
    "0x604928a2A563748A1fFd2bFA670a2CEDb3445692",
    "0xF5861731377be1b1736A29301f87C4Bf907895Db",
    "0x175D5f7aC09D6f9c4B70C11b1cd84671bc3E9F0F",
    "0xCd91F1eBb8E096dAAb1d5c2c97f5E340c6b2E155"
)

fun to4Array(data: Long): Collection<Byte> {
    val buffer = ByteBuffer.allocate(8)
    buffer.putLong(data)
    val bytes = ByteArray(4)
    System.arraycopy(buffer.array(), 4, bytes, 0, 4)
    return bytes.toList()
}

fun hashTransaction(unsignedTransaction: IUnsignedTransaction): String {
    val (timestamp, fee, senderAddress, amount, recipientAddress, data) = unsignedTransaction

    val byteArray = mutableListOf<Byte>().apply {
        addAll(to4Array(timestamp))
        addAll(to4Array(fee))
        addAll(senderAddress.toByteArray(Charsets.UTF_8).toList())
        addAll(to4Array(amount))
        if (recipientAddress != null) {
            addAll(recipientAddress.toByteArray(Charsets.UTF_8).toList())
        }
        if (data != null) {
            addAll(data.toByteArray(Charsets.UTF_8).toList())
        }
    }.toByteArray()

    val sha256 = MessageDigest.getInstance("SHA-256")
    val firstHash = sha256.digest(byteArray)
    val secondHash = sha256.digest(firstHash)

    return secondHash.joinToString("") { "%02x".format(it and 0xFF.toByte()) }
}

data class IUnsignedTransaction(
    val timestamp: Long,
    val fee: Long,
    val senderAddress: String,
    val amount: Long,
    val recipientAddress: String?,
    val data: String?
)

fun generateRandomHexHash(length: Int): String {
    val characters = "0123456789abcdef"
    val random = java.util.Random()
    val hashBuilder = StringBuilder()

    repeat(length) {
        val randomIndex = random.nextInt(characters.length)
        val randomChar = characters[randomIndex]
        hashBuilder.append(randomChar)
    }

    return hashBuilder.toString()
}

