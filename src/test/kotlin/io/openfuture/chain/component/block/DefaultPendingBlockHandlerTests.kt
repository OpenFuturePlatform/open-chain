package io.openfuture.chain.component.block

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.consensus.component.block.BlockApprovalStage
import io.openfuture.chain.consensus.component.block.DefaultPendingBlockHandler
import io.openfuture.chain.consensus.model.entity.transaction.TransferTransaction
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.network.domain.NetworkBlockApproval
import io.openfuture.chain.network.domain.NetworkMainBlock
import io.openfuture.chain.network.service.NetworkService
import org.assertj.core.api.Assertions.assertThat
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
    @Mock private lateinit var networkService: NetworkService

    private lateinit var defaultPendingBlockHandler: DefaultPendingBlockHandler


    @Before
    fun setUp() {
        defaultPendingBlockHandler = DefaultPendingBlockHandler(
            epochService,
            mainBlockService,
            keyHolder,
            networkService
        )
    }

    @Test
    fun addBlockShouldAddMainBlockAndBroadcast() {
        val delegate = Delegate("publicKey", "address", 1)
        val transactions: MutableSet<Transaction> = mutableSetOf(
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
        val block = MainBlock(
            1L,
            "previousHash",
            2L,
            3L,
            "publicKey",
            TransactionUtils.calculateMerkleRoot(transactions),
            transactions
        )
        val privateKey = "529719453390370201f3f0efeeffe4c3a288f39b2e140a3f6074c8d3fc0021e6"
        block.sign(ByteUtils.fromHexString(privateKey))

        given(keyHolder.getPrivateKey()).willReturn(
            ByteUtils.fromHexString(privateKey))
        given(keyHolder.getPublicKey()).willReturn("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4")
        given(epochService.getSlotNumber(block.timestamp)).willReturn(2L)
        given(epochService.getCurrentSlotOwner()).willReturn(delegate)
        given(epochService.getDelegates()).willReturn(
            setOf(Delegate("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4", "address", 1)))
        given(mainBlockService.isValid(block)).willReturn(true)

        defaultPendingBlockHandler.addBlock(block)

        verify(networkService, times(1)).broadcast(any(NetworkMainBlock::class.java))
        verify(networkService, times(1)).broadcast(any(NetworkBlockApproval::class.java))
        assertThat(defaultPendingBlockHandler.pendingBlocks.first()).isEqualTo(block)
    }

    @Test
    fun handleApproveMessageShouldPrepareApproveMessage() {
        val publicKey = "037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"
        val delegate = Delegate(publicKey, "address", 1)
        val transactions: MutableSet<Transaction> = mutableSetOf(
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
        val mainBlock = MainBlock(
            1L,
            "previousHash",
            2L,
            3L,
            publicKey,
            TransactionUtils.calculateMerkleRoot(transactions),
            transactions
        )
        val message = NetworkBlockApproval(
            BlockApprovalStage.PREPARE.value,
            2L,
            "2a897fecaaaddcd924a9f562be1cdacf0c7cf3370d1d13c3209f0d05be6bd26f",
            publicKey,
            "MEUCIQDJ8KF201VgsyrL4geU80Lv+JqqnCRuH1ScxtxJ1mYLVAIgPiB6GUWVD6jB7uk6smJCV7jzCUmK/JkIqhZO0/81q5M="
        )

        val privateKey = "529719453390370201f3f0efeeffe4c3a288f39b2e140a3f6074c8d3fc0021e6"
        mainBlock.sign(ByteUtils.fromHexString(privateKey))

        given(keyHolder.getPrivateKey()).willReturn(
            ByteUtils.fromHexString(privateKey))
        given(keyHolder.getPublicKey()).willReturn(publicKey)
        given(epochService.getSlotNumber(mainBlock.timestamp)).willReturn(2L)
        given(epochService.getCurrentSlotOwner()).willReturn(delegate)
        given(mainBlockService.isValid(mainBlock)).willReturn(true)
        given(epochService.getDelegates()).willReturn(
            setOf(Delegate("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4", "address", 1)))
        defaultPendingBlockHandler.addBlock(mainBlock)

        given(epochService.getDelegates()).willReturn(setOf(delegate))

        defaultPendingBlockHandler.handleApproveMessage(message)

        verify(networkService, times(3)).broadcast(any(NetworkBlockApproval::class.java))
        assertThat(defaultPendingBlockHandler.prepareVotes.get(message.publicKey)).isEqualTo(delegate)
    }

    @Test
    fun handleApproveMessageShouldCommitApproveMessage() {
        val publicKey = "037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"
        val delegate = Delegate(publicKey, "address", 1)
        val message = NetworkBlockApproval(
            BlockApprovalStage.COMMIT.value,
            2L,
            "2a897fecaaaddcd924a9f562be1cdacf0c7cf3370d1d13c3209f0d05be6bd26f",
            publicKey,
            "MEYCIQCjcs54dZxldCIqIwpwKxsUAYIeMGNdlaBudCF7Ps7SYwIhAJiKsUUoOuTiVHZNePFDjPWF5sarUFguNqV+lMDnsieX"
        )

        given(epochService.getDelegates()).willReturn(setOf(delegate))

        defaultPendingBlockHandler.handleApproveMessage(message)

        assertThat(defaultPendingBlockHandler.commits.get(message.hash))
            .isEqualTo(mutableListOf(delegate))
    }

    @Test
    fun handleApproveMessageShouldCommitApproveTwoMessages() {
        addBlockShouldAddMainBlockAndBroadcast()
        val blockHash = "2b9fa527078f6c5d8b48cbee453e138f6a3a54f9ef1da57b7b464bb17a4d7a72"
        val publicKey = "037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"
        val publicKey2 = "020bf4f11983fca4a99b0d7b18fbffa02462c36126757e598e9beaa33a275f0948"
        val delegate = Delegate(publicKey, "address", 1)
        val delegate2 = Delegate(publicKey2, "address2", 2)
        val message = NetworkBlockApproval(
            BlockApprovalStage.COMMIT.value,
            2L,
            blockHash,
            publicKey,
            "MEUCIQDkR3bVd0CSPjKMNwPbBfdvp9elSAHri5bptmEfH8k2xwIgQZ45FZodbc43khzM52guR5iXkI1zxDa2hhGk8sQq+gM="
        )
        val message2 = NetworkBlockApproval(
            BlockApprovalStage.COMMIT.value,
            2L,
            blockHash,
            publicKey2,
            "MEUCIQCFYDosVmdgjhSn59dciBBWoyXcc8/m8z2MVuBFbSu31gIgCuz3/9eufmCmBL8tX5cHgLCnKIS3W6vCCVQNo8QWd+0="
        )

        given(epochService.getDelegates()).willReturn(setOf(delegate, delegate2))

        defaultPendingBlockHandler.handleApproveMessage(message)
        defaultPendingBlockHandler.handleApproveMessage(message2)

        assertThat(defaultPendingBlockHandler.pendingBlocks.size).isEqualTo(1)
        verify(networkService, times(2)).broadcast(any(NetworkBlockApproval::class.java))
        verify(mainBlockService, times(1)).save(any(MainBlock::class.java))
    }

}