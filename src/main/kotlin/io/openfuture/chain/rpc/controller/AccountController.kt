package io.openfuture.chain.rpc.controller

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.rpc.domain.crypto.AccountDto
import io.openfuture.chain.rpc.domain.crypto.ValidateAddressRequest
import io.openfuture.chain.rpc.domain.crypto.WalletDto
import io.openfuture.chain.rpc.domain.crypto.key.DerivationKeyRequest
import io.openfuture.chain.rpc.domain.crypto.key.ImportKeyRequest
import io.openfuture.chain.rpc.domain.crypto.key.KeyDto
import io.openfuture.chain.rpc.domain.crypto.key.RestoreRequest
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/accounts")
class AccountController(
    private val cryptoService: CryptoService,
    private val walletService: WalletService
) {

    @GetMapping("/doGenerate")
    fun generateNewAccount(): AccountDto {
        val seedPhrase = cryptoService.generateSeedPhrase()
        val masterKeys = cryptoService.getMasterKey(seedPhrase)
        val defaultDerivationKey = cryptoService.getDefaultDerivationKey(masterKeys)

        return AccountDto(seedPhrase, KeyDto(masterKeys.ecKey), WalletDto(defaultDerivationKey.ecKey))
    }

    @GetMapping("/wallets/{address}/balance")
    fun getBalance(@PathVariable address: String): Long = walletService.getBalanceByAddress(address)

    @GetMapping("/wallets/{address}/delegates")
    fun getDelegates(@PathVariable address: String): Set<Delegate> = walletService.getVotesByAddress(address)

    @PostMapping("/wallets/validateAddress")
    fun validateAddress(@RequestBody @Valid request: ValidateAddressRequest): ValidateAddressRequest = request

    @PostMapping("/doRestore")
    fun restore(@RequestBody @Valid keyRequest: RestoreRequest): AccountDto {
        val masterKeys = cryptoService.getMasterKey(keyRequest.seedPhrase!!)
        val defaultDerivationKey = cryptoService.getDefaultDerivationKey(masterKeys)

        return AccountDto(keyRequest.seedPhrase!!, KeyDto(masterKeys.ecKey), WalletDto(defaultDerivationKey.ecKey))
    }

    @PostMapping("/doDerive")
    fun getDerivationAccount(@RequestBody @Valid keyRequest: DerivationKeyRequest): WalletDto {
        val masterKeys = cryptoService.getMasterKey(keyRequest.seedPhrase!!)
        val derivationKey = cryptoService.getDerivationKey(masterKeys, keyRequest.derivationPath!!)

        return WalletDto(derivationKey.ecKey)
    }

    @PostMapping("/keys/doPrivateImport")
    fun importPrivateKey(@RequestBody @Valid request: ImportKeyRequest): WalletDto =
        WalletDto(cryptoService.importPrivateKey(request.decodedKey!!))


    @PostMapping("/keys/doExtendedImport")
    fun importExtendedKey(@RequestBody @Valid request: ImportKeyRequest): WalletDto {
        val key = cryptoService.importExtendedKey(request.decodedKey!!)
        val publicKey = cryptoService.serializePublicKey(key)
        val privateKey = if (!key.ecKey.isPrivateEmpty()) cryptoService.serializePrivateKey(key) else null
        return WalletDto(KeyDto(publicKey, privateKey), key.ecKey.getAddress())
    }

    @PostMapping("/keys/doWifImport")
    fun importWifKey(@RequestBody @Valid request: ImportKeyRequest): WalletDto =
        WalletDto(cryptoService.importWifKey(request.decodedKey!!))

}