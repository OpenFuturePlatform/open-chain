package io.openfuture.chain.crypto.service

import io.openfuture.chain.crypto.model.dto.ECKey
import io.openfuture.chain.crypto.model.dto.ExtendedKey
import io.openfuture.chain.rpc.domain.crypto.AccountDto

interface CryptoService {

    fun generateSeedPhrase(): String

    fun generateNewAccount(): AccountDto

    fun getRootAccount(seedPhrase: String): AccountDto

    fun getDerivationKey(seedPhrase: String, derivationPath: String): ExtendedKey

    fun importKey(key: String): ExtendedKey

    fun importWifKey(wifKey: String): ECKey

    fun serializePublicKey(key: ExtendedKey): String

    fun serializePrivateKey(key: ExtendedKey): String

}