package io.openfuture.chain.controller

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.controller.common.BaseController
import io.openfuture.chain.controller.common.RestResponse
import io.openfuture.chain.domain.rpc.HardwareInfo
import io.openfuture.chain.domain.rpc.UptimeResponse
import io.openfuture.chain.domain.rpc.node.NodeTimestampResponse
import io.openfuture.chain.domain.rpc.node.NodeVersionResponse
import io.openfuture.chain.service.HardwareInfoService
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("${PathConstant.RPC}/info")
class NodeInfoController(
    nodeClock: NodeClock,
    private val context: ApplicationContext,
    private val hardwareInfoService: HardwareInfoService
) : BaseController(nodeClock) {

    @GetMapping("/getVersion")
    fun getVersion(): RestResponse {
        return RestResponse(getResponseHeader(), NodeVersionResponse())
    }

    @GetMapping("/getTimestamp")
    fun getTimestamp(): RestResponse {
        return RestResponse(getResponseHeader(), nodeClock.nodeTime())
    }

    @GetMapping("/getUptime")
    fun getUptime(): RestResponse {
        return RestResponse(getResponseHeader(), nodeClock.nodeTime() - context.startupDate)
    }

    @GetMapping("/getHardwareInfo")
    fun getHardwareInfo(): RestResponse {
        return RestResponse(getResponseHeader(), hardwareInfoService.getHardwareInfo())
    }

}