package io.openfuture.chain.core.component

import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.serialization.Serializable
import io.openfuture.chain.network.service.NetworkApiService
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Component
class SyncFetchBlockScheduler(
    private val networkApiService: NetworkApiService,
    private val nodeProperties: NodeProperties
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SyncFetchBlockScheduler::class.java)
    }


    private var index = 0
    private lateinit var listNodeInfo: List<NodeInfo>

    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var future: ScheduledFuture<*>? = null


    fun activate(message: Serializable, listNodeInfo: List<NodeInfo>) {
        deactivate()
        this.listNodeInfo = listNodeInfo
        future = executor.scheduleWithFixedDelay(
            { execute(message) },
            nodeProperties.syncExpiry!!,
            nodeProperties.syncExpiry!!,
            TimeUnit.MILLISECONDS)
    }

    fun deactivate() {
        if (null == future) {
            return
        }

        future!!.cancel(true)
    }

    private fun execute(message: Serializable) {
        if (index < listNodeInfo.size) {
            networkApiService.sendToAddress(message, listNodeInfo[++index])
            return
        }

        index = 0
        networkApiService.sendToAddress(message, listNodeInfo[index])
        log.debug("Send ${ToStringBuilder.reflectionToString(message, ToStringStyle.SHORT_PREFIX_STYLE)}")
    }

}