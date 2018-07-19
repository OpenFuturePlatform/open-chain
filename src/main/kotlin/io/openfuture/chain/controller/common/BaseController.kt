package io.openfuture.chain.controller.common

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.controller.PathConstant
import io.openfuture.chain.property.NodeProperty
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(PathConstant.RPC)
class BaseController(
    protected val nodeClock: NodeClock,
    protected val nodeProperty: NodeProperty
) {

    protected fun getResponseHeader(): ResponseHeader {
        return ResponseHeader(nodeClock.networkTime(), nodeProperty.version!!)
    }

}