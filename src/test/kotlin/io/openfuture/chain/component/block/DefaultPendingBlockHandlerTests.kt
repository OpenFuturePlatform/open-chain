package io.openfuture.chain.component.block

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.consensus.component.block.DefaultPendingBlockHandler
import io.openfuture.chain.consensus.component.block.ObserverStage
import io.openfuture.chain.consensus.component.block.TimeSlotHelper
import io.openfuture.chain.consensus.model.entity.Delegate
import io.openfuture.chain.consensus.model.entity.block.GenesisBlock
import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.consensus.model.entity.transaction.TransferTransaction
import io.openfuture.chain.consensus.service.GenesisBlockService
import io.openfuture.chain.consensus.service.MainBlockService
import io.openfuture.chain.crypto.component.key.NodeKeyHolder
import io.openfuture.chain.network.domain.NetworkBlockApprovalMessage
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import io.openfuture.chain.network.domain.NetworkMainBlock
import io.openfuture.chain.network.service.NetworkService
import org.assertj.core.api.Assertions
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify


class DefaultPendingBlockHandlerTests : ServiceTests() {

    @Mock private lateinit var timeSlotHelper: TimeSlotHelper
    @Mock private lateinit var genesisBlockService: GenesisBlockService
    @Mock private lateinit var mainBlockService: MainBlockService
    @Mock private lateinit var keyHolder: NodeKeyHolder
    @Mock private lateinit var networkService: NetworkService

    private lateinit var defaultPendingBlockHandler: DefaultPendingBlockHandler


    @Before
    fun setUp() {
        defaultPendingBlockHandler = DefaultPendingBlockHandler(
            timeSlotHelper,
            genesisBlockService,
            mainBlockService,
            keyHolder,
            networkService
        )
    }

    @Test
    fun addBlockShouldAddGenesisBlockAndBroadcast() {
        val delegate = Delegate(
            "publicKey",
            "address",
            1
        )
        val block = GenesisBlock(
            1L,
            "previousHash",
            2L,
            "publicKey",
            3L,
            setOf(delegate)
        )
        val privateKey = "529719453390370201f3f0efeeffe4c3a288f39b2e140a3f6074c8d3fc0021e6"
        block.sign(ByteUtils.fromHexString(privateKey))

        given(keyHolder.getPrivateKey()).willReturn(
            ByteUtils.fromHexString(privateKey))
        given(keyHolder.getPublicKey()).willReturn(
            ByteUtils.fromHexString("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"))
        given(timeSlotHelper.getSlotNumber(block.timestamp)).willReturn(1L)
        given(timeSlotHelper.getCurrentSlotOwner()).willReturn(delegate)
        given(genesisBlockService.isValid(block)).willReturn(true)

        defaultPendingBlockHandler.addBlock(block)

        verify(networkService, times(1)).broadcast(any(NetworkGenesisBlock::class.java))
        verify(networkService, times(1)).broadcast(any(NetworkBlockApprovalMessage::class.java))
        Assertions.assertThat(defaultPendingBlockHandler.pendingBlocks.first()).isEqualTo(block)
    }

    @Test
    fun addBlockShouldAddMainBlockAndBroadcast() {
        val delegate = Delegate(
            "publicKey",
            "address",
            1
        )
        val block = MainBlock(
            1L,
            "previousHash",
            2L,
            "publicKey",
            mutableSetOf(
                TransferTransaction(
                    1L,
                    2L,
                    3L,
                    "recipientAddress",
                    "senderAddress",
                    "senderPublicKey",
                    "senderSignature",
                    "hash",
                    null
                )
            )
        )
        val privateKey = "529719453390370201f3f0efeeffe4c3a288f39b2e140a3f6074c8d3fc0021e6"
        block.sign(ByteUtils.fromHexString(privateKey))

        given(keyHolder.getPrivateKey()).willReturn(
            ByteUtils.fromHexString(privateKey))
        given(keyHolder.getPublicKey()).willReturn(
            ByteUtils.fromHexString("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"))
        given(timeSlotHelper.getSlotNumber(block.timestamp)).willReturn(2L)
        given(timeSlotHelper.getCurrentSlotOwner()).willReturn(delegate)
        given(mainBlockService.isValid(block)).willReturn(true)

        defaultPendingBlockHandler.addBlock(block)

        verify(networkService, times(1)).broadcast(any(NetworkMainBlock::class.java))
        verify(networkService, times(1)).broadcast(any(NetworkBlockApprovalMessage::class.java))
        Assertions.assertThat(defaultPendingBlockHandler.pendingBlocks.first()).isEqualTo(block)
    }

    @Test
    fun addBlockShouldAddGenesisBlockAndMainBlockAndBroadcast() {
        val delegate = Delegate(
            "publicKey",
            "address",
            1
        )
        val mainBlock = MainBlock(
            1L,
            "previousHash",
            2L,
            "publicKey",
            mutableSetOf(
                TransferTransaction(
                    1L,
                    2L,
                    3L,
                    "recipientAddress",
                    "senderAddress",
                    "senderPublicKey",
                    "senderSignature",
                    "hash",
                    null
                )
            )
        )
        val genesisBlock = GenesisBlock(
            1L,
            "previousHash",
            2L,
            "publicKey",
            3L,
            setOf(delegate)
        )
        val privateKey = "529719453390370201f3f0efeeffe4c3a288f39b2e140a3f6074c8d3fc0021e6"
        genesisBlock.sign(ByteUtils.fromHexString(privateKey))
        mainBlock.sign(ByteUtils.fromHexString(privateKey))

        given(keyHolder.getPrivateKey()).willReturn(
            ByteUtils.fromHexString(privateKey))
        given(keyHolder.getPublicKey()).willReturn(
            ByteUtils.fromHexString("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"))
        given(timeSlotHelper.getSlotNumber(mainBlock.timestamp)).willReturn(1L, 2L)
        given(timeSlotHelper.getCurrentSlotOwner()).willReturn(delegate)
        given(genesisBlockService.isValid(genesisBlock)).willReturn(true)
        given(mainBlockService.isValid(mainBlock)).willReturn(true)

        defaultPendingBlockHandler.addBlock(genesisBlock)
        defaultPendingBlockHandler.addBlock(mainBlock)

        verify(networkService, times(1)).broadcast(any(NetworkGenesisBlock::class.java))
        verify(networkService, times(1)).broadcast(any(NetworkMainBlock::class.java))
        verify(networkService, times(2)).broadcast(any(NetworkBlockApprovalMessage::class.java))

        Assertions.assertThat(defaultPendingBlockHandler.pendingBlocks.first()).isEqualTo(mainBlock)
    }

    @Test
    fun handleApproveMessageShouldPrepareApproveMessage() {
        val publicKey = "037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"
        val delegate = Delegate(publicKey, "address", 1)
        val genesisBlock = GenesisBlock(
            1L,
            "previousHash",
            2L,
            publicKey,
            3L,
            setOf(delegate)
        )
        val mainBlock = MainBlock(
            1L,
            "previousHash",
            2L,
            publicKey,
            mutableSetOf(
                TransferTransaction(
                    1L,
                    2L,
                    3L,
                    "recipientAddress",
                    "senderAddress",
                    "senderPublicKey",
                    "senderSignature",
                    "hash",
                    null
                )
            )
        )
        val message = NetworkBlockApprovalMessage(
            ObserverStage.PREPARE.value,
            2L,
            "2a897fecaaaddcd924a9f562be1cdacf0c7cf3370d1d13c3209f0d05be6bd26f",
            publicKey,
            "MEUCIDVXCqqrcMuGtHW/m2HEiDM9vcG0wXKA6I0zswsNdtVyAiEA2/orRDpuz0Gp6OI9XsrgW4K8jfFazHbCwqiHq7J1+S8="
        )

        val privateKey = "529719453390370201f3f0efeeffe4c3a288f39b2e140a3f6074c8d3fc0021e6"
        mainBlock.sign(ByteUtils.fromHexString(privateKey))

        given(keyHolder.getPrivateKey()).willReturn(
            ByteUtils.fromHexString(privateKey))
        given(keyHolder.getPublicKey()).willReturn(
            ByteUtils.fromHexString(publicKey))
        given(timeSlotHelper.getSlotNumber(mainBlock.timestamp)).willReturn(2L)
        given(timeSlotHelper.getCurrentSlotOwner()).willReturn(delegate)
        given(mainBlockService.isValid(mainBlock)).willReturn(true)
        given(genesisBlockService.getLast()).willReturn(genesisBlock)
        defaultPendingBlockHandler.addBlock(mainBlock)

        defaultPendingBlockHandler.handleApproveMessage(message)

        verify(networkService, times(3)).broadcast(any(NetworkBlockApprovalMessage::class.java))
        Assertions.assertThat(defaultPendingBlockHandler.prepareVotes.get(message.publicKey)).isEqualTo(delegate)
    }

}