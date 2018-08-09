package io.openfuture.chain.crypto.service

import io.openfuture.chain.crypto.component.key.DerivationKeysHelper
import io.openfuture.chain.crypto.component.key.ExtendedKeyDeserializer
import io.openfuture.chain.crypto.component.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.component.key.PrivateKeyManager
import io.openfuture.chain.crypto.component.seed.SeedCalculator
import io.openfuture.chain.crypto.component.seed.SeedPhraseGenerator
import io.openfuture.chain.crypto.model.dictionary.PhraseLength
import io.openfuture.chain.crypto.model.dto.ECKey
import io.openfuture.chain.crypto.model.dto.ExtendedKey
import io.openfuture.chain.crypto.validation.SeedPhraseValidator
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service

@Service
class DefaultCryptoService(
    private val seedPhraseGenerator: SeedPhraseGenerator,
    private val keyManager: PrivateKeyManager,
    private val serializer: ExtendedKeySerializer,
    private val deserializer: ExtendedKeyDeserializer,
    private val derivationKeysHelper: DerivationKeysHelper,
    private val seedPhraseValidator: SeedPhraseValidator,
    private val seedCalculator: SeedCalculator
) : CryptoService {

    override fun generateSeedPhrase(): String = seedPhraseGenerator.createSeedPhrase(PhraseLength.TWELVE)

    override fun getMasterKey(seedPhrase: String): ExtendedKey {
        if (!seedPhraseValidator.isValid(seedPhrase))
            throw IllegalArgumentException("Invalid seed phrase")

        return ExtendedKey.root(seedCalculator.calculateSeed(seedPhrase))
    }

    override fun getDerivationKey(masterKeys: ExtendedKey, derivationPath: String): ExtendedKey =
        derivationKeysHelper.derive(masterKeys, derivationPath)

    override fun getDefaultDerivationKey(masterKeys: ExtendedKey): ExtendedKey =
        derivationKeysHelper.derive(masterKeys, DerivationKeysHelper.DEFAULT_DERIVATION_KEY)

    override fun isValidAddress(address: String, publicKey: ByteArray): Boolean =
        address == ECKey(publicKey, false).getAddress()

    override fun importPrivateKey(privateKey: String): ECKey {
        val ecKey = ECKey(ByteUtils.fromHexString(privateKey), true)
        if (privateKey != ByteUtils.toHexString(ecKey.getPrivate())) {
            throw IllegalArgumentException("Invalid private key")
        }

        return ecKey
    }

    override fun importExtendedKey(extendedKey: String): ExtendedKey = deserializer.deserialize(extendedKey)

    override fun importWifKey(wifKey: String): ECKey = keyManager.importPrivateKey(wifKey)

    override fun serializePublicKey(key: ExtendedKey): String = serializer.serializePublic(key)

    override fun serializePrivateKey(key: ExtendedKey): String = serializer.serializePrivate(key)

}
