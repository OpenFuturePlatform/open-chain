package io.openfuture.chain.rpc.controller

import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.service.state.DefaultDelegateStateService.Companion.DEFAULT_DELEGATE_RATING
import io.openfuture.chain.crypto.annotation.AddressChecksum
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.crypto.AccountDto
import io.openfuture.chain.rpc.domain.crypto.ValidateAddressRequest
import io.openfuture.chain.rpc.domain.crypto.WalletDto
import io.openfuture.chain.rpc.domain.crypto.key.DerivationKeyRequest
import io.openfuture.chain.rpc.domain.crypto.key.ImportKeyRequest
import io.openfuture.chain.rpc.domain.crypto.key.KeyDto
import io.openfuture.chain.rpc.domain.crypto.key.RestoreRequest
import io.openfuture.chain.rpc.domain.vote.VotesResponse
import org.springframework.data.domain.PageImpl
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors
import javax.validation.Valid

@RestController
@Validated
@RequestMapping("/rpc/accounts")
class AccountController(
    private val cryptoService: CryptoService,
    private val walletStateService: WalletStateService,
    private val walletVoteService: WalletVoteService,
    private val delegateStateService: DelegateStateService,
    private val delegateService: DelegateService,
    private val voteTransactionService: VoteTransactionService
) {

    @GetMapping("/doGenerate")
    fun generateNewAccount(): AccountDto {
        val seedPhrase = cryptoService.generateSeedPhrase()
        val masterKeys = cryptoService.getMasterKey(seedPhrase)
        val defaultDerivationKey = cryptoService.getDefaultDerivationKey(masterKeys)

        return AccountDto(seedPhrase, KeyDto(masterKeys.ecKey), WalletDto(defaultDerivationKey.ecKey))
    }

    @GetMapping("/wallets/{address}/balance")
    fun getBalance(@PathVariable @AddressChecksum address: String): Long =
        walletStateService.getActualBalanceByAddress(address)

    @GetMapping("/wallets/{address}/delegates")
    fun getDelegates(@PathVariable @AddressChecksum address: String, request: PageRequest): PageResponse<VotesResponse> {
        val delegates = walletVoteService.getVotesByAddress(address)
            .map {
                val state = delegateStateService.getLastByAddress(it.id.delegateKey)
                val delegate = delegateService.getByPublicKey(it.id.delegateKey)
                VotesResponse(
                    delegate.address,
                    delegate.publicKey,
                    state?.rating ?: DEFAULT_DELEGATE_RATING,
                    walletVoteService.getVotesForDelegate(it.id.delegateKey).size,
                    voteTransactionService.getLastVoteForDelegate(address, it.id.delegateKey).header.timestamp,
                    voteTransactionService.getUnconfirmedBySenderAgainstDelegate(address, it.id.delegateKey) != null
                )
            }

        val pageActiveDelegate = delegates.stream()
            .skip(request.offset)
            .limit(request.getLimit().toLong())
            .collect(Collectors.toList())

        return PageResponse(PageImpl(pageActiveDelegate, request, delegates.size.toLong()))
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