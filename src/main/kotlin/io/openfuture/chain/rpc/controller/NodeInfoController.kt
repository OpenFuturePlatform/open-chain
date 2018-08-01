package io.openfuture.chain.rpc.controller

import io.openfuture.chain.consensus.service.HardwareInfoService
import io.openfuture.chain.rpc.controller.base.BaseController
import io.openfuture.chain.rpc.domain.RestResponse
import io.openfuture.chain.rpc.domain.node.HardwareInfo
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/rpc/info")
class NodeInfoController(
    private val nodeClock: NodeClock,
    private val context: ApplicationContext,
    private val hardwareInfoService: HardwareInfoService) {

    @GetMapping("/getVersion")
    fun getVersion() { }

    @GetMapping("/getUptime")
    fun getUptime(): Long {
        return nodeClock.nodeTime() - context.startupDate
    }

    @GetMapping("/getHardwareInfo")
    fun getHardwareInfo(): HardwareInfo {
        return hardwareInfoService.getHardwareInfo()
    }

}