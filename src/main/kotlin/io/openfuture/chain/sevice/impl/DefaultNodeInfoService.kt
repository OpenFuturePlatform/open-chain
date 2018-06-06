package io.openfuture.chain.sevice.impl

import io.openfuture.chain.domain.NodeHardwareResponse
import io.openfuture.chain.sevice.NodeInfoService
import org.springframework.stereotype.Service
import oshi.SystemInfo

@Service
class DefaultNodeInfoService : NodeInfoService {

    override fun getHardwareInfo(): NodeHardwareResponse {
        val runtime = Runtime.getRuntime()

        val availableProcessors = runtime.availableProcessors()
        val freeMemory = runtime.freeMemory()
        val maxMemory = runtime.maxMemory()

        return NodeHardwareResponse(availableProcessors, freeMemory, maxMemory)
    }

}