package io.openfuture.chain.smartcontract.deploy.execution

import io.openfuture.chain.smartcontract.core.model.SmartContract
import io.openfuture.chain.smartcontract.deploy.ContractProperties
import io.openfuture.chain.smartcontract.deploy.domain.ClassSource
import io.openfuture.chain.smartcontract.deploy.domain.ContractDto
import io.openfuture.chain.smartcontract.deploy.domain.ContractMethod
import io.openfuture.chain.smartcontract.deploy.exception.ContractExecutionException
import io.openfuture.chain.smartcontract.deploy.exception.ContractLoadingException
import io.openfuture.chain.smartcontract.deploy.load.ContractInjector
import io.openfuture.chain.smartcontract.deploy.load.SourceClassLoader
import org.apache.commons.lang3.reflect.MethodUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicInteger


@Component
class ContractExecutor(
    private val properties: ContractProperties
) {

    companion object {
        private val uniqueIdentifier = AtomicInteger(0)
    }

    private val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1)
    private val classLoader = SourceClassLoader()


    fun run(contract: ContractDto, method: ContractMethod, executionFee: Long): ExecutionResult {
        val availableTime = executionFee / properties.millisecondCost!!
        val executionTime = Math.min(availableTime, properties.maxExecutionTime!!)
        val instance = loadClassAndState(contract)
        val threadName = "${instance::class.java.simpleName}-${uniqueIdentifier.getAndIncrement()}"
        val result = ExecutionResult(threadName, instance as SmartContract, null)

        val task = executeMethod(instance, method, threadName)
        try {
            val startTime = System.currentTimeMillis()
            result.output = task.get(executionTime, MILLISECONDS)
            val endTime = System.currentTimeMillis()

            result.surplus = (availableTime - (endTime - startTime)) * properties.millisecondCost!!
            return result
        } catch (ex: Exception) {
            task.cancel(true)
            throw ContractExecutionException(ex.message, ex)
        }
    }

    private fun executeMethod(instance: SmartContract, method: ContractMethod, identifier: String): Future<Any> {
        return pool.submit (Callable {
            Thread.currentThread().name = identifier
            val paramTypes = instance.javaClass.declaredMethods.firstOrNull { it.name == method.name }?.parameterTypes
            return@Callable MethodUtils.invokeExactMethod(instance, method.name, method.params, paramTypes)
        })
    }

    private fun loadClassAndState(contract: ContractDto): Any {
        try {
            var instance = classLoader.getLoadedClass(contract.clazz)?.newInstance()

            if (null == instance) {
                instance = classLoader.loadBytes(ClassSource(contract.bytes)).clazz.newInstance()
            }

            val injector = ContractInjector(instance as SmartContract, classLoader)
            injector.injectState(contract.state)
            injector.injectFields(contract.address, contract.owner)
            return instance
        } catch (ex: Throwable) {
            throw ContractLoadingException("Error while loading contract and state: ${ex.message}", ex)
        }
    }

    data class ExecutionResult(
        var identifier: String,
        var instance: SmartContract?,
        var output: Any?,
        var surplus: Long = 0
    )

}