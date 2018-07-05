package io.openfuture.chain.controller

import io.openfuture.chain.domain.crypto.AccountDto
import io.openfuture.chain.domain.crypto.RootAccountDto
import io.openfuture.chain.domain.crypto.key.*
import io.openfuture.chain.service.CryptoService
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("${PathConstant.RPC}/crypto")
class CryptoController(
    private val cryptoService: CryptoService
) {

    @PostMapping("/doGenerateMaster")
    fun getMasterKey(@RequestBody @Valid keyRequest: MasterKeyRequest): KeyDto {
        val key = cryptoService.getMasterKey(keyRequest.seedPhrase!!)

        return KeyDto(
            cryptoService.serializePublicKey(key),
            cryptoService.serializePrivateKey(key)
        )
    }

    @PostMapping("/doDerive")
    fun getDerivationKey(@RequestBody @Valid keyRequest: DerivationKeyRequest): AccountDto {
        val key = cryptoService.getDerivationKey(keyRequest.seedPhrase!!, keyRequest.derivationPath!!)

        return AccountDto(
            cryptoService.serializePublicKey(key),
            cryptoService.serializePrivateKey(key),
            key.ecKey.getAddress()
        )
    }

    @GetMapping("/generate")
    fun generateKey(): RootAccountDto = cryptoService.generateKey()

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
    fun importWifKey(@RequestBody @Valid request: ImportKeyRequest): AccountDto = AccountDto(
        cryptoService.importWifKey(request.decodedKey!!)
    )

}