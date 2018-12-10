package io.openfuture.chain.smartcontract.deploy.execution

import io.openfuture.chain.smartcontract.deploy.domain.ClassSource
import io.openfuture.chain.smartcontract.deploy.domain.ContractDto
import io.openfuture.chain.smartcontract.deploy.domain.ContractMethod
import io.openfuture.chain.smartcontract.deploy.exception.ContractExecutionException
import io.openfuture.chain.smartcontract.deploy.exception.ContractLoadingException
import io.openfuture.chain.smartcontract.deploy.load.SourceClassLoader
import org.apache.commons.lang3.reflect.FieldUtils
import org.apache.commons.lang3.reflect.MethodUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class ContractExecutor {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ContractExecutor::class.java)
        private val uniqueIdentifier = AtomicInteger(0)
    }

    private val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1)
    private val classLoader = SourceClassLoader()


    fun run(initiatorAddress: String, contract: ContractDto, method: ContractMethod): ExecutionResult {
        var output: Any? = null
        var exception: Throwable? = null
        val threadName = "${contract.clazz}-${uniqueIdentifier.getAndIncrement()}"
        val completionLatch = CountDownLatch(1)
        pool.execute {
            Thread.currentThread().name = threadName
            try {
                val instance = loadClassAndState(contract)
                val fullInput = (method.params.toMutableList() + initiatorAddress).toTypedArray()
                val fullClassInput = (method.params.map { it.javaClass } + String::class.java).toTypedArray()

                output = MethodUtils.invokeExactMethod(instance, method.name, fullInput, fullClassInput)
            } catch (ex: Throwable) {
                log.debug("Error while executing (${contract.clazz} - ${method.name}): ${ex.message}")
                exception = ex
            }
            completionLatch.countDown()
            Thread.sleep(1000)
        }
        completionLatch.await()

        if (null == exception) {
            //todo save state?
            return ExecutionResult(threadName, output)
        } else {
            throw ContractExecutionException(exception!!.message, exception)
        }
    }

    private fun loadClassAndState(contract: ContractDto): Any {
        try {
            val instance = (Class.forName(contract.clazz) ?: classLoader.loadBytes(ClassSource(contract.bytes)).clazz)
                .newInstance()
            //todo load state
            FieldUtils.writeField(instance, "address", contract.address, true)
            FieldUtils.writeField(instance, "owner", contract.owner, true)
            return instance
        } catch (ex: Throwable) {
            throw ContractLoadingException("Error while loading contract and state: ${ex.message}", ex)
        }
    }

    data class ExecutionResult(
        val identifier: String,
        val output: Any?
    )

}