package io.openfuture.chain.rpc.controller.base

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class MainController {

    @RequestMapping(value = ["/", "/login", "/upload", "/private-key",
        "/new", "/new/restore", "/new/seed-phrase", "/new/keys", "/new/create", "/new/complete",
        "/wallet", "/wallet/transactions", "/wallet/delegates/vote", "/wallet/delegates/become-a-delegate"])
    fun frontend(): String = "frontend"

}