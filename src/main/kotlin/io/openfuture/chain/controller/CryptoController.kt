package io.openfuture.chain.controller

import io.openfuture.chain.domain.crypto.AccountDto
import io.openfuture.chain.domain.crypto.RootAccountDto
import io.openfuture.chain.domain.crypto.key.DerivationKeyRequest
import io.openfuture.chain.domain.crypto.key.ImportKeyRequest
import io.openfuture.chain.domain.crypto.key.RestoreRequest
import io.openfuture.chain.service.CryptoService
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("${PathConstant.RPC}/crypto")
class CryptoController(
    private val cryptoService: CryptoService
) {

    @PostMapping("/doRestore")
    fun restore(@RequestBody @Valid keyRequest: RestoreRequest): RootAccountDto =
        cryptoService.getRootAccount(keyRequest.seedPhrase!!)

    @PostMapping("/doDerive")
    fun getDerivationAccount(@RequestBody @Valid keyRequest: DerivationKeyRequest): AccountDto {
        val key = cryptoService.getDerivationKey(keyRequest.seedPhrase!!, keyRequest.derivationPath!!)

        return AccountDto(
            cryptoService.serializePublicKey(key),
            cryptoService.serializePrivateKey(key),
            key.ecKey.getAddress()
        )
    }

    @GetMapping("/doGenerate")
    fun generateNewAccount(): RootAccountDto = cryptoService.generateNewAccount()

    @PostMapping("/keys/doImport")
    fun importKey(@RequestBody @Valid request: ImportKeyRequest): AccountDto {
        val importedKey = cryptoService.importKey(request.decodedKey!!)

        return AccountDto(
            cryptoService.serializePublicKey(importedKey),
            if (!importedKey.ecKey.isPrivateEmpty()) cryptoService.serializePrivateKey(importedKey) else null,
            importedKey.ecKey.getAddress()
        )
    }

    @PostMapping("/keys/doImportWif")
    fun importWifKey(@RequestBody @Valid request: ImportKeyRequest): AccountDto =
        AccountDto(cryptoService.importWifKey(request.decodedKey!!))

}