package io.openfuture.chain.controller

import io.openfuture.chain.domain.crypto.DerivationKeyRequest
import io.openfuture.chain.domain.crypto.MasterKeyRequest
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
    fun getMasterKey(@RequestBody keyRequest: MasterKeyRequest): KeyDto {
        val key = cryptoService.getMasterKey(keyRequest.seedPhrase)

        return KeyDto(
                cryptoService.serializedPublicKey(key),
                cryptoService.serializedPrivateKey(key)
        )
    }

    @PostMapping("/doDerive")
    fun getDerivationKey(@RequestBody keyRequest: DerivationKeyRequest): AddressKeyDto {
        val key = cryptoService.getDerivationKey(keyRequest.seedPhrase, keyRequest.derivationPath)

        return AddressKeyDto(
                cryptoService.serializedPublicKey(key),
                cryptoService.serializedPrivateKey(key),
                key.ecKey.getAddress()
        )
    }

    @GetMapping("/generate")
    fun generateKey() = cryptoService.generateKey()

    @PostMapping("/keys/doImport")
    fun importKey(@RequestBody @Valid request: ImportKeyRequest): AddressKeyDto {
        val importedKey = cryptoService.importKey(request.decodedKey!!)
        return AddressKeyDto(
            cryptoService.serializePublicKey(importedKey),
            if (!importedKey.ecKey.isPrivateEmpty()) cryptoService.serializePrivateKey(importedKey) else null,
            importedKey.ecKey.getAddress()
        )
    }

    @PostMapping("/keys/doImportWif")
    fun importWifKey(@RequestBody @Valid request: ImportKeyRequest): AddressKeyDto = AddressKeyDto(
        cryptoService.importWifKey(request.decodedKey!!)
    )

}