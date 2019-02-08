package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.repository.TransferTransactionRepository
import io.openfuture.chain.core.repository.UTransferTransactionRepository
import io.openfuture.chain.core.service.AccountStateService
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.network.message.core.AccountStateMessage
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.smartcontract.deploy.calculation.ContractCostCalculator
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.`when`
import org.mockito.BDDMockito.anyString
import org.mockito.Mock
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.util.ReflectionTestUtils

@RunWith(SpringRunner::class)
class DefaultTransactionServiceTest {

    @Mock
    private lateinit var repository: TransferTransactionRepository

    @Mock
    private lateinit var uRepository: UTransferTransactionRepository

    @Mock
    private lateinit var contractSevice: ContractService

    @Mock
    private lateinit var accountStateService: AccountStateService

    @SpyBean
    private lateinit var contractCostCalculator: ContractCostCalculator

    private lateinit var transactionService: DefaultTransferTransactionService

    private lateinit var transactionMessage: TransferTransactionMessage

    private val delegateAddress = "delegateAddress"

    private val senderAddress = "senderAddress"

    private var contractCost: Long = 0L


    @Before
    fun setUp() {
        transactionService = DefaultTransferTransactionService(repository, uRepository, contractSevice, contractCostCalculator)
        ReflectionTestUtils.setField(transactionService, "accountStateService", accountStateService, AccountStateService::class.java)
        val code = this::class.java.getResourceAsStream("/classes/JavaContract.class").readBytes()
        val bytecode = ByteUtils.toHexString(code)
        contractCost = contractCostCalculator.calculateCost(code)
        transactionMessage = TransferTransactionMessage(121325454, 0, senderAddress, "", "", "", 0, null, bytecode)

    }

    @Test
    fun processShouldReturnErrorResultReceipt() {
        val sendFee = 10L
        val expectedSize = 1
        val expectedError = "Contract is not deployed."
        transactionMessage.fee = sendFee

        `when`(accountStateService.updateBalanceByAddress(senderAddress, -sendFee)).thenReturn(AccountStateMessage(senderAddress, 0))
        `when`(accountStateService.updateBalanceByAddress(delegateAddress, sendFee)).thenReturn(AccountStateMessage(delegateAddress, 0))

        val result = transactionService.process(transactionMessage, delegateAddress)
        assertEquals(result.getResults().size, expectedSize)
        assertEquals(result.getResults().first().error, expectedError)
    }

    @Test
    fun processShouldReturnTwoResultReceipt() {
        val sendFee = 40L
        val expectedSize = 2
        val expectedDelivery = sendFee - contractCost
        transactionMessage.fee = sendFee

        `when`(contractSevice.generateAddress(senderAddress)).thenReturn(senderAddress)
        `when`(accountStateService.updateBalanceByAddress(senderAddress, -sendFee)).thenReturn(AccountStateMessage(senderAddress, 0))
        `when`(accountStateService.updateBalanceByAddress(delegateAddress, sendFee)).thenReturn(AccountStateMessage(delegateAddress, 0))
        `when`(accountStateService.updateStorage(anyString(), anyString())).thenReturn(AccountStateMessage(anyString(), 0))

        val result = transactionService.process(transactionMessage, delegateAddress)
        val expectedSenderReceiptResult = result.getResults().find { it.from == senderAddress }
        val expectedDelegateReceiptResult = result.getResults().find { it.from == delegateAddress }

        assertEquals(result.getResults().size, expectedSize)

        assertEquals(expectedSenderReceiptResult?.to, delegateAddress)
        assertEquals(expectedSenderReceiptResult?.amount, sendFee)
        assertTrue(expectedSenderReceiptResult?.error.isNullOrBlank())
        assertTrue(expectedSenderReceiptResult?.data.isNullOrBlank())

        assertEquals(expectedDelegateReceiptResult?.to, senderAddress)
        assertEquals(expectedDelegateReceiptResult?.amount, expectedDelivery)
        assertTrue(expectedDelegateReceiptResult?.error.isNullOrBlank())
        assertTrue(expectedDelegateReceiptResult?.error.isNullOrBlank())
    }

    @Test
    fun processShouldReturnOneResultReceipt() {
        val sendFee = 30L
        val expectedSize = 1
        transactionMessage.fee = sendFee

        `when`(contractSevice.generateAddress(senderAddress)).thenReturn(senderAddress)
        `when`(accountStateService.updateBalanceByAddress(senderAddress, -sendFee)).thenReturn(AccountStateMessage(senderAddress, 0))
        `when`(accountStateService.updateBalanceByAddress(delegateAddress, sendFee)).thenReturn(AccountStateMessage(delegateAddress, 0))
        `when`(accountStateService.updateStorage(anyString(), anyString())).thenReturn(AccountStateMessage(anyString(), 0))

        val result = transactionService.process(transactionMessage, delegateAddress)
        val expectedReceiptResult = result.getResults().first()

        assertEquals(result.getResults().size, expectedSize)
        assertEquals(expectedReceiptResult.from, senderAddress)
        assertEquals(expectedReceiptResult.to, delegateAddress)
        assertEquals(expectedReceiptResult.amount, sendFee)
        assertTrue(expectedReceiptResult.error.isNullOrBlank())
        assertTrue(expectedReceiptResult.data.isNullOrBlank())
    }

}