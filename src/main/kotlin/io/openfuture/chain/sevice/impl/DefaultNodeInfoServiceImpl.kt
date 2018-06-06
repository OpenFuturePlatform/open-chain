package io.openfuture.chain.sevice.impl

import io.openfuture.chain.domain.NodeHardwareResponse
import io.openfuture.chain.sevice.NodeInfoService
import org.springframework.stereotype.Service
import oshi.SystemInfo

@Service
class DefaultNodeInfoServiceImpl : NodeInfoService {

    override fun getHardwareInfo(): NodeHardwareResponse {
        val systemInfo = SystemInfo()
        val hardwareLayer = systemInfo.hardware

        val processor = hardwareLayer.processor
        val memory = hardwareLayer.memory
        val sensors = hardwareLayer.sensors

        val cpuCoreCount = processor.physicalProcessorCount
        val cpuTemperature = sensors.cpuTemperature
        val ramTotal = memory.total

        return NodeHardwareResponse(cpuCoreCount, cpuTemperature, ramTotal)
    }

}