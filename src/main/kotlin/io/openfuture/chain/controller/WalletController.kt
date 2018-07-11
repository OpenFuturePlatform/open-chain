package io.openfuture.chain.controller

import io.openfuture.chain.service.WalletService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${PathConstant.RPC}/wallets")
class WalletController(
    private val walletService: WalletService
) {

    @GetMapping("/{address}/balance")
    fun getBalance(@PathVariable address: String): Double = walletService.getBalance(address)

}