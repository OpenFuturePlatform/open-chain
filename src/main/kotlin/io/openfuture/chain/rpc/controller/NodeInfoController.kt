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
    private val context: ApplicationContext,
    private val hardwareInfoService: HardwareInfoService
) : BaseController() {

    @GetMapping("/getVersion")
    fun getVersion(): RestResponse<String> {
        return RestResponse(getResponseHeader(), nodeProperties.version!!)
    }

    @GetMapping("/getTimestamp")
    fun getTimestamp(): RestResponse<Long> {
        return RestResponse(getResponseHeader(), nodeClock.nodeTime())
    }

    @GetMapping("/getUptime")
    fun getUptime(): RestResponse<Long> {
        return RestResponse(getResponseHeader(), nodeClock.nodeTime() - context.startupDate)
    }

    @GetMapping("/getHardwareInfo")
    fun getHardwareInfo(): RestResponse<HardwareInfo> {
        return RestResponse(getResponseHeader(), hardwareInfoService.getHardwareInfo())
    }

}