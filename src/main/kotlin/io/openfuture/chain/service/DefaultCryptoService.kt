package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.crypto.key.DerivationKeysHelper
import io.openfuture.chain.crypto.key.ExtendedKeyDeserializer
import io.openfuture.chain.crypto.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.key.PrivateKeyManager
import io.openfuture.chain.crypto.seed.PhraseLength
import io.openfuture.chain.crypto.seed.calculator.SeedCalculator
import io.openfuture.chain.crypto.seed.generator.SeedPhraseGenerator
import io.openfuture.chain.crypto.seed.validator.SeedPhraseValidator
import io.openfuture.chain.domain.crypto.AccountDto
import io.openfuture.chain.domain.crypto.RootAccountDto
import io.openfuture.chain.domain.crypto.key.KeyDto
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

    override fun generateNewAccount(): RootAccountDto = getRootAccount(generateSeedPhrase())

    override fun getRootAccount(seedPhrase: String): RootAccountDto {
        if (!seedPhraseValidator.isValid(seedPhrase))
            throw IllegalArgumentException("Invalid seed phrase")

        val rootExtendedKey = ExtendedKey.root(seedCalculator.calculateSeed(seedPhrase))
        val extendedKey = derivationKeysHelper.deriveDefaultAddress(rootExtendedKey)

        return RootAccountDto(
            seedPhrase,
            KeyDto(serializer.serializePublic(rootExtendedKey), serializer.serializePrivate(rootExtendedKey)),
            AccountDto(
                KeyDto(serializer.serializePublic(extendedKey), serializer.serializePrivate(extendedKey)),
                extendedKey.ecKey.getAddress()
            )
        )
    }

    override fun getDerivationKey(seedPhrase: String, derivationPath: String): ExtendedKey {
        if (!seedPhraseValidator.isValid(seedPhrase))
            throw IllegalArgumentException("Invalid seed phrase")

        val masterKey = ExtendedKey.root(seedCalculator.calculateSeed(seedPhrase))
        return derivationKeysHelper.derive(masterKey, derivationPath)
    }

    override fun importKey(key: String): ExtendedKey = deserializer.deserialize(key)

    override fun importWifKey(wifKey: String): ECKey = keyManager.importPrivateKey(wifKey)

    override fun serializePublicKey(key: ExtendedKey): String = serializer.serializePublic(key)

    override fun serializePrivateKey(key: ExtendedKey): String = serializer.serializePrivate(key)

}
