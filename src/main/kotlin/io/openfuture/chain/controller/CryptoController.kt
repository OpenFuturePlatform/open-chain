package io.openfuture.chain.controller

import io.openfuture.chain.service.CryptoService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${PathConstant.RPC}/keys")
class CryptoController(
    val cryptoService: CryptoService
)