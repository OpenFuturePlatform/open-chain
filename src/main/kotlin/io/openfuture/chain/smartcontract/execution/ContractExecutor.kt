package io.openfuture.chain.smartcontract.execution

import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.smartcontract.component.load.SmartContractLoader
import io.openfuture.chain.smartcontract.model.ExecutionContext
import io.openfuture.chain.smartcontract.model.SmartContract
import io.openfuture.chain.smartcontract.property.ContractProperties
import io.openfuture.chain.smartcontract.util.SerializationUtils
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


    fun run(contract: String, message: TransferTransactionMessage, delegateAddress: String): ExecutionResult {
        val persistedContract = contractService.getByAddress(message.recipientAddress!!)
        val smartContractLoader = SmartContractLoader(this::class.java.classLoader)
        smartContractLoader.loadClass(ByteUtils.fromHexString(persistedContract.bytecode))
        val instance = SerializationUtils.deserialize<SmartContract>(ByteUtils.fromHexString(contract), smartContractLoader)
        val threadName = "${instance::class.java.simpleName}-${uniqueIdentifier.getAndIncrement()}"

        val task = executeMethod(instance, threadName, message, delegateAddress, persistedContract.cost)
        return try {
            task.get(properties.maxExecutionTime!!, MILLISECONDS)
        } catch (ex: TimeoutException) {
            task.cancel(true)
            val results = handleException("Contract execution timeout", instance, message, delegateAddress, persistedContract.cost)
            ExecutionResult(results, contract)
        }
    }

    private fun executeMethod(instance: SmartContract, identifier: String, message: TransferTransactionMessage, delegateAddress: String, contractCost: Long): Future<ExecutionResult> {
        return pool.submit(Callable {
            Thread.currentThread().name = identifier
            return@Callable proceedExecution(instance, message, delegateAddress, contractCost)
        })
    }

    private fun proceedExecution(instance: SmartContract, message: TransferTransactionMessage, delegateAddress: String, contractCost: Long): ExecutionResult {
        val context = ExecutionContext(message.amount, message.senderAddress)
        val contextField = instance.javaClass.superclass.getDeclaredField("executionContext")
        contextField.isAccessible = true
        contextField.set(instance, context)

        return try {
            val paramTypes = instance.javaClass.declaredMethods.firstOrNull { it.name == message.data}?.parameterTypes
            val output = MethodUtils.invokeExactMethod(instance, message.data, emptyArray(), paramTypes)
            contextField.set(instance, null)

            val results = assembleResults(context, instance, message, delegateAddress, contractCost)
            val serializedState = ByteUtils.toHexString(SerializationUtils.serialize(instance))

            ExecutionResult(results, serializedState, output)
        } catch (ex: InvocationTargetException) {
            val results = handleException(ex.targetException.message ?: "", instance, message, delegateAddress, contractCost)
            ExecutionResult(results)
        }
    }

    private fun assembleResults(context: ExecutionContext, instance: SmartContract, message: TransferTransactionMessage, delegateAddress: String, contractCost: Long): List<ReceiptResult> {
        val receiptResults = mutableListOf<ReceiptResult>()
        context.getTransfers().forEach {
            val result = ReceiptResult(message.senderAddress, it.recipientAddress, it.amount)
            receiptResults.add(result)
        }

        val spentFunds = context.getSpentFunds()
        if (spentFunds < context.amount) {
            receiptResults.add(ReceiptResult(message.senderAddress, instance.address, context.amount - spentFunds))
        }

        val processReceipt = ReceiptResult(message.senderAddress, delegateAddress, message.fee - contractCost)
        receiptResults.add(processReceipt)
        return receiptResults
    }

    private fun handleException(errorMessage: String, instance: SmartContract, message: TransferTransactionMessage, delegateAddress: String, contractCost: Long): List<ReceiptResult> {
        val errorReceipt = ReceiptResult(message.senderAddress, instance.address, 0, error = errorMessage)
        val processReceipt = ReceiptResult(message.senderAddress, delegateAddress, message.fee - contractCost)
        return listOf(errorReceipt, processReceipt)
    }

}