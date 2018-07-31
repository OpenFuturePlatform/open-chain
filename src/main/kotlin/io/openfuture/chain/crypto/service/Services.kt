package io.openfuture.chain.crypto.service

import io.openfuture.chain.crypto.model.dto.ECKey
import io.openfuture.chain.crypto.model.dto.ExtendedKey

interface CryptoService {

    fun generateSeedPhrase(): String

    fun getMasterKeys(seedPhrase: String): ExtendedKey

    fun getDerivationKey(masterKeys: ExtendedKey, derivationPath: String): ExtendedKey

    fun getDefaultDerivationKey(masterKeys: ExtendedKey): ExtendedKey

    fun importKey(key: String): ExtendedKey

    fun importWifKey(wifKey: String): ECKey

    fun serializePublicKey(key: ExtendedKey): String

    fun serializePrivateKey(key: ExtendedKey): String

}