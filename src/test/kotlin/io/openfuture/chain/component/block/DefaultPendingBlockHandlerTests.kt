package io.openfuture.chain.component.block

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.consensus.component.block.BlockApprovalStage
import io.openfuture.chain.consensus.component.block.DefaultPendingBlockHandler
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.sync.ChainSynchronizer
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.RewardTransactionMessage
import io.openfuture.chain.network.service.NetworkApiService
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify


class DefaultPendingBlockHandlerTests : ServiceTests() {

    @Mock private lateinit var epochService: EpochService
    @Mock private lateinit var mainBlockService: MainBlockService
    @Mock private lateinit var keyHolder: NodeKeyHolder
    @Mock private lateinit var networkService: NetworkApiService
    @Mock private lateinit var chainSynchronizer: ChainSynchronizer
    @Mock private lateinit var properties: ConsensusProperties

    private lateinit var defaultPendingBlockHandler: DefaultPendingBlockHandler


    @Before
    fun setUp() {
        defaultPendingBlockHandler = DefaultPendingBlockHandler(
            epochService,
            mainBlockService,
            keyHolder,
            networkService,
            chainSynchronizer,
            properties
        )
    }

    @Test
    fun addBlockShouldAddMainBlockAndBroadcast() {
        val payload = MainBlockPayload("merkleHash", "stateHash", "receiptHash")
        val privateKey = "529719453390370201f3f0efeeffe4c3a288f39b2e140a3f6074c8d3fc0021e6"
        val rewardTransactionMessage = createRewardTransactionMessage(1L)
        val pendingBlock = PendingBlockMessage(2L, "previousHash", 1L, "hash",
            "signature", "publicKey", payload.merkleHash, payload.stateHash, payload.receiptHash,
            rewardTransactionMessage, listOf(), listOf(), listOf(), listOf(), listOf(), listOf())
        val block = MainBlock(1, 1, "previousHash", "hash", "signature", "publicKey", payload)
        val delegate = DelegateState("publicKey", block, 1, "address", 1)

        given(keyHolder.getPrivateKey()).willReturn(
            ByteUtils.fromHexString(privateKey))
        given(keyHolder.getPublicKeyAsHexString()).willReturn("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4")
        given(epochService.getSlotNumber(pendingBlock.timestamp)).willReturn(2L)
        given(epochService.getCurrentSlotOwner()).willReturn(delegate.address)
        given(epochService.getDelegatesPublicKeys()).willReturn(listOf("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"))
        given(mainBlockService.verify(pendingBlock)).willReturn(true)

        defaultPendingBlockHandler.addBlock(pendingBlock)

        verify(networkService, times(1)).broadcast(any(PendingBlockMessage::class.java))
        verify(networkService, times(1)).broadcast(any(BlockApprovalMessage::class.java))
    }

    @Test
    fun handleApproveMessageShouldPrepareApproveMessage() {
        val privateKey = "237a1f68f5e6ee331c2d1fac4107807f7eaf7eed2265490d9a9a330c3549a43d"
        val publicKey = "020bf4f11983fca4a99b0d7b18fbffa02462c36126757e598e9beaa33a275f0948"
        val hash = "22c626c74fdc7aa6b2809d88a60068e6017a3d7015113ebd0af18cdf9f3809c6"
        val payload = MainBlockPayload("merkleHash", "stateHash", "receiptHash")
        val rewardTransactionMessage = createRewardTransactionMessage(1L)
        val pendingBlock = PendingBlockMessage(2L, "previousHash", 1L, hash,
            "signature", publicKey, payload.merkleHash, payload.stateHash, payload.receiptHash,
            rewardTransactionMessage, listOf(), listOf(), listOf(), listOf(), listOf(), listOf())

        val message = BlockApprovalMessage(
            BlockApprovalStage.PREPARE.value,
            hash,
            publicKey,
            null
        )

        message.signature = SignatureUtils.sign(message.getBytes(), ByteUtils.fromHexString(privateKey))

        given(keyHolder.getPrivateKey()).willReturn(ByteUtils.fromHexString(privateKey))
        given(keyHolder.getPublicKeyAsHexString()).willReturn(publicKey)
        given(epochService.getSlotNumber(pendingBlock.timestamp)).willReturn(2L)
        given(epochService.getCurrentSlotOwner()).willReturn(publicKey)
        given(mainBlockService.verify(pendingBlock)).willReturn(true)
        given(epochService.getDelegatesPublicKeys()).willReturn(
            listOf("020bf4f11983fca4a99b0d7b18fbffa02462c36126757e598e9beaa33a275f0948"))
        defaultPendingBlockHandler.addBlock(pendingBlock)

        given(epochService.getDelegatesPublicKeys()).willReturn(listOf(publicKey))

        defaultPendingBlockHandler.handleApproveMessage(message)

        verify(networkService, times(3)).broadcast(any(BlockApprovalMessage::class.java))
    }

    @Test
    fun handleApproveMessageShouldCommitApproveMessage() {
        val publicKey = "037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"
        val message = BlockApprovalMessage(
            BlockApprovalStage.COMMIT.value,
            "2a897fecaaaddcd924a9f562be1cdacf0c7cf3370d1d13c3209f0d05be6bd26f",
            publicKey,
            "MEYCIQCjcs54dZxldCIqIwpwKxsUAYIeMGNdlaBudCF7Ps7SYwIhAJiKsUUoOuTiVHZNePFDjPWF5sarUFguNqV+lMDnsieX"
        )

        given(epochService.getDelegatesPublicKeys()).willReturn(listOf(publicKey))

        defaultPendingBlockHandler.handleApproveMessage(message)
    }

    @Test
    fun handleApproveMessageShouldCommitApproveTwoMessages() {
        addBlockShouldAddMainBlockAndBroadcast()
        val blockHash = "2b9fa527078f6c5d8b48cbee453e138f6a3a54f9ef1da57b7b464bb17a4d7a72"
        val privateKey = "237a1f68f5e6ee331c2d1fac4107807f7eaf7eed2265490d9a9a330c3549a43d"
        val privateKey2 = "a3ddaee50c7986bb95816b0d389eadbff39146af546fa795a6433c67af97c6c9"
        val publicKey = "020bf4f11983fca4a99b0d7b18fbffa02462c36126757e598e9beaa33a275f0948"
        val publicKey2 = "02fb3085f5f8bd7a095198211fb1d4781fd7f0643dec52328151b6cb46e46931fd"
        val message = BlockApprovalMessage(
            BlockApprovalStage.COMMIT.value,
            blockHash,
            publicKey,
            null
        )
        message.signature = SignatureUtils.sign(message.getBytes(), ByteUtils.fromHexString(privateKey))
        val message2 = BlockApprovalMessage(
            BlockApprovalStage.COMMIT.value,
            blockHash,
            publicKey2,
            null
        )
        message2.signature = SignatureUtils.sign(message2.getBytes(), ByteUtils.fromHexString(privateKey2))

        given(epochService.getDelegatesPublicKeys()).willReturn(listOf(publicKey, publicKey2))

        defaultPendingBlockHandler.handleApproveMessage(message)
        defaultPendingBlockHandler.handleApproveMessage(message2)

        verify(networkService, times(2)).broadcast(any(BlockApprovalMessage::class.java))
    }

    private fun createRewardTransactionMessage(timestamp: Long): RewardTransactionMessage =
        RewardTransactionMessage(timestamp, 0, "senderAddress", "hash",
            "senderSignature", "senderPublicKey", 10,
            "recipientAddress")

}