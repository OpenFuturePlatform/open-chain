package io.openfuture.chain.smartcontract.deploy.execution

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class ContractExecutor {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ContractExecutor::class.java)
    }

    private val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1)


    fun run(runnableClass: String, method: String, vararg input: Any): Result {
        var output: Any? = null
        var exception: Throwable? = null
        val completionLatch = CountDownLatch(1)
        pool.execute {
            try {
                log.debug("Execution started for $runnableClass - $method")
                val clazz = Class.forName(runnableClass)
                val instance = clazz.newInstance()
                output = clazz.getDeclaredMethod(method, *input.map { it.javaClass }.toTypedArray()).invoke(instance, *input)
            } catch (ex: Throwable) {
                log.debug("Error while executing ($runnableClass - $method): ${ex.message}")
                exception = ex
            }
            log.debug("Execution completed for $runnableClass - $method")
            completionLatch.countDown()
        }

        completionLatch.await()
        return Result(runnableClass, output, exception)
    }

    data class Result(
        val identifier: String,
        val output: Any?,
        val exception: Throwable?
    )
}