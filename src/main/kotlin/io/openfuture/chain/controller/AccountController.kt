package io.openfuture.chain.controller

import io.openfuture.chain.domain.rpc.crypto.AccountDto
import io.openfuture.chain.domain.rpc.crypto.WalletDto
import io.openfuture.chain.domain.rpc.crypto.key.DerivationKeyRequest
import io.openfuture.chain.domain.rpc.crypto.key.ImportKeyRequest
import io.openfuture.chain.domain.rpc.crypto.key.KeyDto
import io.openfuture.chain.domain.rpc.crypto.key.RestoreRequest
import io.openfuture.chain.service.CryptoService
import io.openfuture.chain.service.WalletService
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("${PathConstant.RPC}/accounts")
class AccountController(
    private val cryptoService: CryptoService,
    private val walletService: WalletService
) {

    @GetMapping("/doGenerate")
    fun generateNewAccount(): AccountDto = cryptoService.generateNewAccount()

    @PostMapping("/doRestore")
    fun restore(@RequestBody @Valid keyRequest: RestoreRequest): AccountDto =
        cryptoService.getRootAccount(keyRequest.seedPhrase!!)

    @PostMapping("/doDerive")
    fun getDerivationAccount(@RequestBody @Valid keyRequest: DerivationKeyRequest): WalletDto {
        val key = cryptoService.getDerivationKey(keyRequest.seedPhrase!!, keyRequest.derivationPath!!)
        val publicKey = cryptoService.serializePublicKey(key)
        val privateKey = cryptoService.serializePrivateKey(key)
        return WalletDto(KeyDto(publicKey, privateKey), key.ecKey.getAddress())
    }

    @PostMapping("/keys/doImport")
    fun importKey(@RequestBody @Valid request: ImportKeyRequest): WalletDto {
        val key = cryptoService.importKey(request.decodedKey!!)
        val publicKey = cryptoService.serializePublicKey(key)
        val privateKey = if (!key.ecKey.isPrivateEmpty()) cryptoService.serializePrivateKey(key) else null
        return WalletDto(KeyDto(publicKey, privateKey), key.ecKey.getAddress())
    }

    @PostMapping("/keys/doImportWif")
    fun importWifKey(@RequestBody @Valid request: ImportKeyRequest): WalletDto =
        WalletDto(cryptoService.importWifKey(request.decodedKey!!))

    @GetMapping("/wallets/{address}/balance")
    fun getBalance(@PathVariable address: String): Double = walletService.getBalance(address)

}