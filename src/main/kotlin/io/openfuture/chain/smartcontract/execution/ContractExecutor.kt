package io.openfuture.chain.smartcontract.execution

import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.core.util.SerializationUtils
import io.openfuture.chain.smartcontract.component.load.SmartContractLoader
import io.openfuture.chain.smartcontract.model.ExecutionContext
import io.openfuture.chain.smartcontract.model.SmartContract
import io.openfuture.chain.smartcontract.property.ContractProperties
import org.apache.commons.lang3.reflect.MethodUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Component
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

@Component
class ContractExecutor(
    private val properties: ContractProperties,
    private val contractService: ContractService
) {

    companion object {
        private val uniqueIdentifier = AtomicInteger(0)
    }

    private val pool = Executors.newSingleThreadExecutor()


    fun run(contract: String, tx: TransferTransaction, delegateAddress: String): ExecutionResult {
        val persistedContract = contractService.getByAddress(tx.getPayload().recipientAddress!!)
        val smartContractLoader = SmartContractLoader(this::class.java.classLoader)
        smartContractLoader.loadClass(ByteUtils.fromHexString(persistedContract.bytecode))
        val instance = SerializationUtils.deserialize<SmartContract>(ByteUtils.fromHexString(contract), smartContractLoader)
        val threadName = "${instance::class.java.simpleName}-${uniqueIdentifier.getAndIncrement()}"

        val task = executeMethod(instance, threadName, tx, delegateAddress, persistedContract.cost)
        return try {
            task.get(properties.maxExecutionTime!!, MILLISECONDS)
        } catch (ex: TimeoutException) {
            task.cancel(true)
            val results = handleException("Contract execution timeout", instance, tx, delegateAddress, persistedContract.cost)
            ExecutionResult(results, contract)
        }
    }

    private fun executeMethod(instance: SmartContract, identifier: String, tx: TransferTransaction,
                              delegateAddress: String, contractCost: Long): Future<ExecutionResult> {
        return pool.submit(Callable {
            Thread.currentThread().name = identifier
            return@Callable proceedExecution(instance, tx, delegateAddress, contractCost)
        })
    }

    private fun proceedExecution(instance: SmartContract, tx: TransferTransaction, delegateAddress: String,
                                 contractCost: Long): ExecutionResult {
        val context = ExecutionContext(tx.getPayload().amount, tx.senderAddress)
        val contextField = instance.javaClass.superclass.getDeclaredField("executionContext")
        contextField.isAccessible = true
        contextField.set(instance, context)

        return try {
            val paramTypes = instance.javaClass.declaredMethods.firstOrNull { it.name == tx.getPayload().data }?.parameterTypes
            val output = MethodUtils.invokeExactMethod(instance, tx.getPayload().data, emptyArray(), paramTypes)
            contextField.set(instance, null)

            val results = assembleResults(context, instance, tx, delegateAddress, contractCost)
            val serializedState = ByteUtils.toHexString(SerializationUtils.serialize(instance))

            ExecutionResult(results, serializedState, output)
        } catch (ex: InvocationTargetException) {
            val results = handleException(ex.targetException.message ?: "", instance, tx, delegateAddress, contractCost)
            ExecutionResult(results)
        }
    }

    private fun assembleResults(context: ExecutionContext, instance: SmartContract, tx: TransferTransaction,
                                delegateAddress: String, contractCost: Long): List<ReceiptResult> {
        val receiptResults = mutableListOf<ReceiptResult>()
        context.getTransfers().forEach {
            val result = ReceiptResult(tx.senderAddress, it.recipientAddress, it.amount)
            receiptResults.add(result)
        }

        val spentFunds = context.getSpentFunds()
        if (spentFunds < context.amount) {
            receiptResults.add(ReceiptResult(tx.senderAddress, instance.address, context.amount - spentFunds))
        }

        val processReceipt = ReceiptResult(tx.senderAddress, delegateAddress, tx.fee)
        val changeReceipt = ReceiptResult(delegateAddress, tx.senderAddress, tx.fee - contractCost)
        receiptResults.add(processReceipt)
        receiptResults.add(changeReceipt)
        return receiptResults
    }

    private fun handleException(errorMessage: String, instance: SmartContract, tx: TransferTransaction,
                                delegateAddress: String, contractCost: Long): List<ReceiptResult> {
        val errorReceipt = ReceiptResult(tx.senderAddress, instance.address, 0, error = errorMessage)
        val processReceipt = ReceiptResult(tx.senderAddress, delegateAddress, tx.fee - contractCost)
        return listOf(errorReceipt, processReceipt)
    }

}