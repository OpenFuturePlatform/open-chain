package io.openfuture.chain.smartcontract.deploy.execution

import io.openfuture.chain.smartcontract.component.load.SmartContractLoader
import io.openfuture.chain.smartcontract.deploy.domain.ContractDto
import io.openfuture.chain.smartcontract.deploy.domain.ContractMethod
import io.openfuture.chain.smartcontract.deploy.exception.ContractExecutionException
import io.openfuture.chain.smartcontract.model.SmartContract
import io.openfuture.chain.smartcontract.property.ContractProperties
import io.openfuture.chain.smartcontract.util.SerializationUtils
import org.apache.commons.lang3.reflect.MethodUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicInteger


@Component
class ContractExecutor(
    private val properties: ContractProperties
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ContractExecutor::class.java)
        private val uniqueIdentifier = AtomicInteger(0)
    }

    private val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1)


    fun run(contract: ContractDto, method: ContractMethod): ExecutionResult {
        SmartContractLoader().loadClass(contract.bytes)
        var exception: Throwable? = null
        val threadName = "${contract.clazz}-${uniqueIdentifier.getAndIncrement()}"
        val result = ExecutionResult(threadName, null, null)

        val task = pool.submit {
            Thread.currentThread().name = threadName
            try {
                val instance = loadClassAndState(contract)
                result.instance = instance
                result.output = executeMethod(instance, method)
            } catch (ex: Throwable) {
                log.debug("Error while executing (${contract.clazz} - ${method.name}): ${ex.message}")
                exception = ex
            }
        }

        try {
            task.get(properties.executionTimeout!!, MILLISECONDS)
        } catch (ex: Exception) {
            throw ContractExecutionException(ex.message, ex)
        }

        if (null == exception) {
            return result
        } else {
            throw ContractExecutionException(exception!!.message, exception)
        }
    }

    private fun executeMethod(instance: SmartContract, method: ContractMethod): Any? {
        val paramTypes = instance.javaClass.declaredMethods.firstOrNull { it.name == method.name }?.parameterTypes
        return MethodUtils.invokeExactMethod(instance, method.name, method.params, paramTypes)
    }

    private fun loadClassAndState(contract: ContractDto): SmartContract =
        SerializationUtils.deserialize(contract.state)

    data class ExecutionResult(
        var identifier: String,
        var instance: SmartContract?,
        var output: Any?
    )

}