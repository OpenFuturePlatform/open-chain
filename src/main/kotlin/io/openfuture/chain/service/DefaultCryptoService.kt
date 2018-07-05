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
import io.openfuture.chain.domain.crypto.key.AddressKeyDto
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

    override fun importKey(key: String): ExtendedKey = deserializer.deserialize(key)

    override fun importWifKey(wifKey: String): ECKey = keyManager.importPrivateKey(wifKey)

    override fun serializePublicKey(key: ExtendedKey): String = serializer.serializePublic(key)

    override fun serializePrivateKey(key: ExtendedKey): String = serializer.serializePrivate(key)

    override fun generateKey(): AccountDto {
        val seedPhrase = seedPhraseGenerator.createSeedPhrase(PhraseLength.TWELVE)
        val rootExtendedKey = ExtendedKey.root(seedCalculator.calculateSeed(seedPhrase))

        val extendedKey = derivationKeysHelper.deriveDefaultAddress(rootExtendedKey)
        val addressKeyDto = AddressKeyDto(serializer.serializePublic(extendedKey),
            serializer.serializePrivate(extendedKey), extendedKey.ecKey.getAddress())

        return AccountDto(seedPhrase, serializer.serializePublic(rootExtendedKey),
            serializer.serializePrivate(rootExtendedKey), addressKeyDto)
    }

    override fun getMasterKey(seedPhrase: String): ExtendedKey {
        seedPhraseValidator.validate(seedPhrase)

        val seed = seedCalculator.calculateSeed(seedPhrase)
        return ExtendedKey.root(seed)
    }

    override fun getDerivationKey(seedPhrase: String, derivationPath: String): ExtendedKey {
        seedPhraseValidator.validate(seedPhrase)

        val masterKey = getMasterKey(seedPhrase)
        return derivationKeysHelper.derive(masterKey, derivationPath)
    }

}
