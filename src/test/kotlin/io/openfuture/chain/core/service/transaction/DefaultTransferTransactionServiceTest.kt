package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.repository.TransferTransactionRepository
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.service.transaction.confirmed.DefaultTransferTransactionService
import io.openfuture.chain.smartcontract.deploy.calculation.ContractCostCalculator
import io.openfuture.chain.smartcontract.execution.ContractExecutor
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.springframework.test.util.ReflectionTestUtils

class DefaultTransferTransactionServiceTest : ServiceTests() {

    @Mock private lateinit var repository: TransferTransactionRepository
    @Mock private lateinit var contractService: ContractService
    @Mock private lateinit var contractExecutor: ContractExecutor
    @Mock private lateinit var contractCostCalculator: ContractCostCalculator
    @Mock private lateinit var stateManager: StateManager

    private lateinit var transactionService: TransferTransactionService
    private lateinit var transferTransaction: TransferTransaction

    private val delegateAddress = "delegateAddress"
    private val senderAddress = "senderAddress"
    private var contractCost: Long = 30L


    @Before
    fun setUp() {
        transactionService = DefaultTransferTransactionService(repository, contractService, contractCostCalculator,
            contractExecutor)
        ReflectionTestUtils.setField(transactionService, "stateManager", stateManager, StateManager::class.java)
        val code = this::class.java.getResourceAsStream("/classes/JavaContract.class").readBytes()
        val bytecode = ByteUtils.toHexString(code)
        transferTransaction = TransferTransaction(121325454, 0, senderAddress, "", "", "",
            TransferTransactionPayload(0, null, bytecode))

        given(contractCostCalculator.calculateCost(code)).willReturn(contractCost)
    }

    @Test
    fun processWhenFeeIsNotEnoughShouldReturnErrorResultReceipt() {
        val sendFee = 10L
        val expectedSize = 1
        val expectedError = "Contract is not deployed. The fee was charged, but this is not enough for deploy."
        transferTransaction.fee = sendFee

        val result = transactionService.process(transferTransaction, delegateAddress)

        assertEquals(expectedSize, result.getResults().size)
        assertEquals(expectedError, result.getResults().first().error)

        verify(stateManager).updateWalletBalanceByAddress(senderAddress, -sendFee)
        verify(stateManager).updateWalletBalanceByAddress(delegateAddress, sendFee)
    }

    @Test
    fun processWhenFeeIsGreaterThanContractCostShouldReturnTwoResultReceipt() {
        val sendFee = 40L
        val expectedSize = 2
        val expectedDelivery = sendFee - contractCost
        transferTransaction.fee = sendFee

        given(contractService.generateAddress(senderAddress)).willReturn(senderAddress)

        val result = transactionService.process(transferTransaction, delegateAddress)
        val actualSenderReceiptResult = result.getResults().find { it.from == senderAddress }
        val actualDelegateReceiptResult = result.getResults().find { it.from == delegateAddress }

        assertEquals(expectedSize, result.getResults().size)

        assertEquals(delegateAddress, actualSenderReceiptResult?.to)
        assertEquals(sendFee, actualSenderReceiptResult?.amount)
        assertTrue(actualSenderReceiptResult?.error.isNullOrBlank())
        assertTrue(actualSenderReceiptResult?.data!!.isNotEmpty())

        assertEquals(senderAddress, actualDelegateReceiptResult?.to)
        assertEquals(expectedDelivery, actualDelegateReceiptResult?.amount)
        assertTrue(actualDelegateReceiptResult?.error.isNullOrBlank())
        assertTrue(actualDelegateReceiptResult?.data.isNullOrBlank())

        verify(stateManager).updateSmartContractStorage(anyString(), anyString())
        verify(stateManager).updateWalletBalanceByAddress(senderAddress, -contractCost)
        verify(stateManager).updateWalletBalanceByAddress(delegateAddress, contractCost)
    }

    @Test
    fun processWhenFeeIsEqualContractCostShouldReturnOneResultReceipt() {
        val sendFee = 30L
        val expectedSize = 1
        transferTransaction.fee = sendFee

        given(contractService.generateAddress(senderAddress)).willReturn(senderAddress)

        val result = transactionService.process(transferTransaction, delegateAddress)
        val actualReceiptResult = result.getResults().first()

        assertEquals(expectedSize, result.getResults().size)
        assertEquals(senderAddress, actualReceiptResult.from)
        assertEquals(delegateAddress, actualReceiptResult.to)
        assertEquals(sendFee, actualReceiptResult.amount)
        assertTrue(actualReceiptResult.error.isNullOrBlank())
        assertTrue(actualReceiptResult.data!!.isNotEmpty())

        verify(stateManager).updateSmartContractStorage(anyString(), anyString())
        verify(stateManager).updateWalletBalanceByAddress(senderAddress, -contractCost)
        verify(stateManager).updateWalletBalanceByAddress(delegateAddress, contractCost)
    }

}