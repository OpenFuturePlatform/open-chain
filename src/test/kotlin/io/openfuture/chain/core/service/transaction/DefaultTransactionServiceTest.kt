package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.core.repository.TransferTransactionRepository
import io.openfuture.chain.core.repository.UTransferTransactionRepository
import io.openfuture.chain.core.service.AccountStateService
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.network.message.core.TransferTransactionMessage
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

class DefaultTransactionServiceTest : ServiceTests() {

    @Mock private lateinit var repository: TransferTransactionRepository
    @Mock private lateinit var uRepository: UTransferTransactionRepository
    @Mock private lateinit var contractService: ContractService
    @Mock private lateinit var accountStateService: AccountStateService
    @Mock private lateinit var contractExecutor: ContractExecutor
    @Mock private lateinit var contractCostCalculator: ContractCostCalculator

    private lateinit var transactionService: DefaultTransferTransactionService
    private lateinit var transactionMessage: TransferTransactionMessage
    private val delegateAddress = "delegateAddress"
    private val senderAddress = "senderAddress"
    private var contractCost: Long = 30L


    @Before
    fun setUp() {
        transactionService = DefaultTransferTransactionService(repository, uRepository, contractService,
            contractCostCalculator, contractExecutor)
        ReflectionTestUtils.setField(transactionService, "accountStateService", accountStateService, AccountStateService::class.java)
        val code = this::class.java.getResourceAsStream("/classes/JavaContract.class").readBytes()
        val bytecode = ByteUtils.toHexString(code)
        transactionMessage = TransferTransactionMessage(121325454, 0, senderAddress, "", "", "", 0, null, bytecode)

        given(contractCostCalculator.calculateCost(code)).willReturn(contractCost)
    }

    @Test
    fun processWhenFeeIsNotEnoughShouldReturnErrorResultReceipt() {
        val sendFee = 10L
        val expectedSize = 1
        val expectedError = "Contract is not deployed. The fee was charged, but this is not enough for deploy."
        transactionMessage.fee = sendFee

        val result = transactionService.process(transactionMessage, delegateAddress)
        assertEquals(result.getResults().size, expectedSize)
        assertEquals(result.getResults().first().error, expectedError)
    }

    @Test
    fun processWhenFeeIsGreaterThanContractCostShouldReturnTwoResultReceipt() {
        val sendFee = 40L
        val expectedSize = 2
        val expectedDelivery = sendFee - contractCost
        transactionMessage.fee = sendFee

        given(contractService.generateAddress(senderAddress)).willReturn(senderAddress)

        val result = transactionService.process(transactionMessage, delegateAddress)
        val actualSenderReceiptResult = result.getResults().find { it.from == senderAddress }
        val actualDelegateReceiptResult = result.getResults().find { it.from == delegateAddress }

        assertEquals(result.getResults().size, expectedSize)

        assertEquals(delegateAddress, actualSenderReceiptResult?.to)
        assertEquals(sendFee, actualSenderReceiptResult?.amount)
        assertTrue(actualSenderReceiptResult?.error.isNullOrBlank())
        assertTrue(actualSenderReceiptResult?.data!!.isNotEmpty())

        assertEquals(senderAddress, actualDelegateReceiptResult?.to)
        assertEquals(expectedDelivery, actualDelegateReceiptResult?.amount)
        assertTrue(actualDelegateReceiptResult?.error.isNullOrBlank())
        assertTrue(actualDelegateReceiptResult?.error.isNullOrBlank())

        verify(accountStateService).updateStorage(anyString(), anyString())
        verify(accountStateService).updateBalanceByAddress(senderAddress, -contractCost)
        verify(accountStateService).updateBalanceByAddress(delegateAddress, contractCost)
    }

    @Test
    fun processWhenFeeIsEqualContractCostShouldReturnOneResultReceipt() {
        val sendFee = 30L
        val expectedSize = 1
        transactionMessage.fee = sendFee

        given(contractService.generateAddress(senderAddress)).willReturn(senderAddress)

        val result = transactionService.process(transactionMessage, delegateAddress)
        val expectedReceiptResult = result.getResults().first()

        assertEquals(result.getResults().size, expectedSize)
        assertEquals(expectedReceiptResult.from, senderAddress)
        assertEquals(expectedReceiptResult.to, delegateAddress)
        assertEquals(expectedReceiptResult.amount, sendFee)
        assertTrue(expectedReceiptResult.data!!.isNotEmpty())
        assertTrue(expectedReceiptResult.error.isNullOrBlank())

        verify(accountStateService).updateStorage(anyString(), anyString())
        verify(accountStateService).updateBalanceByAddress(senderAddress, -contractCost)
        verify(accountStateService).updateBalanceByAddress(delegateAddress, contractCost)
    }

}