package io.openfuture.chain.network.sync.impl

import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.util.ByteConstants
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.network.sync.SyncBlockResponseHandler
import io.openfuture.chain.network.sync.SyncManager
import io.openfuture.chain.network.sync.impl.SynchronizationStatus.NOT_SYNCHRONIZED
import io.openfuture.chain.network.sync.impl.SynchronizationStatus.PROCESSING
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

@Component
class SyncBlockScheduler(
    private val nodeProperties: NodeProperties,
    private val syncBlockHandler: SyncBlockResponseHandler,
    private val syncManager: SyncManager,
    private val networkApiService: NetworkApiService,
    private val nodeClock: NodeClock,
    private val transferTransactionService: TransferTransactionService,
    private val cryptoService: CryptoService
) {

    @Scheduled(fixedRateString = "\${node.synchronization-interval}")
    fun syncBlock() {
        if (networkApiService.isChannelsEmpty()) {
            return
        }

        if (syncManager.getSyncStatus() == NOT_SYNCHRONIZED || (syncManager.getSyncStatus() == PROCESSING && isResponseTimeOut())) {
            syncBlockHandler.synchronize()
        }
    }


    @Scheduled(fixedRateString = "3000")
    fun createTx() {
        val date = nodeClock.networkTime()
        val fee = 1L
        val senderAddress = "0x6b19b2dEE50f8D8b6c903b3A369dF00291e3c405"
        val amount = 1L
        val recipientAddress = "0x82d80f07F137322a4BFbfB109DEeecd46c7cB45C"

        val hash = createHash(TransactionHeader( date, fee, senderAddress),
            TransferTransactionPayload(amount, recipientAddress))

        val privateKey = ByteUtils.fromHexString("2aab27b94b561a1cedb4f24f7525a4404fbf9f8fa9709ac3411d4e5c476389f9")
        val publicKey = "0383a5113554f8f7d5ee87652a747ab0614f0921f3836c90f17f56924154e3b9c7"
        val siangture = SignatureUtils.sign(ByteUtils.fromHexString(hash), privateKey)

        val delegateRequest = TransferTransactionRequest(date, fee, hash, senderAddress, amount, recipientAddress,
            siangture, publicKey)

        if (cryptoService.isValidAddress(senderAddress, ByteUtils.fromHexString(publicKey))) {
            transferTransactionService.add(delegateRequest)
        }


    }

    private fun isResponseTimeOut() = System.currentTimeMillis() - syncBlockHandler.getLastResponseTime() > nodeProperties.synchronizationResponseDelay!!

    private fun createHash(header: TransactionHeader, payload: TransactionPayload): String {
        val bytes = ByteBuffer.allocate(ByteConstants.LONG_BYTES + ByteConstants.LONG_BYTES + header.senderAddress.toByteArray(StandardCharsets.UTF_8).size + payload.getBytes().size)
            .put(header.getBytes())
            .put(payload.getBytes())
            .array()

        return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

}