package io.openfuture.chain.controller

import io.openfuture.chain.controller.common.BaseController
import io.openfuture.chain.controller.common.RestResponse
import io.openfuture.chain.domain.rpc.HardwareInfo
import io.openfuture.chain.service.HardwareInfoService
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("${PathConstant.RPC}/info")
class NodeInfoController(
    private val context: ApplicationContext,
    private val hardwareInfoService: HardwareInfoService
) : BaseController() {

    @GetMapping("/getVersion")
    fun getVersion(): RestResponse<String> {
        return RestResponse(getResponseHeader(), nodeProperties.version)
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