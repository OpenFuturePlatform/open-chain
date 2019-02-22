package io.openfuture.chain.rpc.controller

import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.crypto.annotation.AddressChecksum
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.rpc.domain.crypto.AccountDto
import io.openfuture.chain.rpc.domain.crypto.ValidateAddressRequest
import io.openfuture.chain.rpc.domain.crypto.WalletDto
import io.openfuture.chain.rpc.domain.crypto.key.DerivationKeyRequest
import io.openfuture.chain.rpc.domain.crypto.key.ImportKeyRequest
import io.openfuture.chain.rpc.domain.crypto.key.KeyDto
import io.openfuture.chain.rpc.domain.crypto.key.RestoreRequest
import io.openfuture.chain.rpc.domain.vote.VoteResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@Validated
@RequestMapping("/rpc/accounts")
class AccountController(
    private val cryptoService: CryptoService,
    private val stateManager: StateManager,
    private val transactionManager: TransactionManager
) {

    @GetMapping("/doGenerate")
    fun generateNewAccount(): AccountDto {
        val seedPhrase = cryptoService.generateSeedPhrase()
        val masterKeys = cryptoService.getMasterKey(seedPhrase)
        val defaultDerivationKey = cryptoService.getDefaultDerivationKey(masterKeys)

        return AccountDto(seedPhrase, KeyDto(masterKeys.ecKey), WalletDto(defaultDerivationKey.ecKey))
    }

    @GetMapping("/wallets/{address}/balance")
    fun getBalance(@PathVariable @AddressChecksum address: String): Long {
        val balance = stateManager.getWalletBalanceByAddress(address)
        val unconfirmedBalance = transactionManager.getUnconfirmedBalanceBySenderAddress(address)

        return balance - unconfirmedBalance
    }

    @GetMapping("/wallets/{address}/delegate")
    fun getDelegates(@PathVariable @AddressChecksum address: String): VoteResponse? {
        val accountState = stateManager.getByAddress<AccountState>(address)

        if (null != accountState.voteFor) {
            val delegate = stateManager.getByAddress<DelegateState>(accountState.voteFor!!)

            return VoteResponse(
                delegate.walletAddress,
                delegate.address,
                delegate.rating,
                stateManager.getVotesForDelegate(delegate.address).size,
                transactionManager.getLastVoteForDelegate(address, delegate.address).timestamp,
                transactionManager.getUnconfirmedVoteBySenderAgainstDelegate(address, delegate.address) != null
            )
        }

        return null
    }

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