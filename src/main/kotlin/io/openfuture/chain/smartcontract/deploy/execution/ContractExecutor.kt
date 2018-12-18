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
    private val classLoader = SourceClassLoader()


    fun run(contract: ContractDto, method: ContractMethod): ExecutionResult {
        var exception: Throwable? = null
        val threadName = "${contract.clazz}-${uniqueIdentifier.getAndIncrement()}"
        val result = ExecutionResult(threadName, null, null)

        val task = pool.submit {
            Thread.currentThread().name = threadName
            try {
                val instance = loadClassAndState(contract)
                result.instance = instance as SmartContract
                result.output = MethodUtils.invokeExactMethod(instance, method.name, method.params,
                    method.params.map { it::class.javaPrimitiveType ?: it::class.javaObjectType }.toTypedArray())
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
        var output: Any?
    )

}