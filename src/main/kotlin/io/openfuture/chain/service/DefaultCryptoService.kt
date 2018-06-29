package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.crypto.key.DerivationKeysHelper
import io.openfuture.chain.crypto.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.seed.PhraseLength.TWELVE
import io.openfuture.chain.crypto.seed.calculator.SeedCalculator
import io.openfuture.chain.crypto.seed.generator.SeedPhraseGenerator
import io.openfuture.chain.crypto.seed.validator.SeedPhraseValidator
import io.openfuture.chain.domain.crypto.key.AddressKeyDto
import io.openfuture.chain.domain.crypto.key.WalletDto
import org.springframework.stereotype.Service

@Service
class DefaultCryptoService(
    private val seedPhraseGenerator: SeedPhraseGenerator,
    private val seedCalculator: SeedCalculator,
    private val derivationKeyHelper: DerivationKeysHelper,
    private val extendedKeySerializer: ExtendedKeySerializer,
    private val seedPhraseValidator: SeedPhraseValidator,
    private val derivationKeysHelper: DerivationKeysHelper
) : CryptoService {

    override fun generateSeedPhrase(): String = seedPhraseGenerator.createSeedPhrase(TWELVE)

    override fun getMasterKey(seedPhrase: String): ExtendedKey {
        seedPhraseValidator.validate(seedPhrase)

        val seed = seedCalculator.calculateSeed(seedPhrase)
        return ExtendedKey.root(seed)
    }

    override fun getDerivationKey(seedPhrase: String, derivationPath: String): ExtendedKey {
        seedPhraseValidator.validate(seedPhrase)

        val masterKey = getMasterKey(seedPhrase)
        return derivationKeyHelper.derive(masterKey, derivationPath)
    }

    override fun serializedPublicKey(key: ExtendedKey): String = extendedKeySerializer.serializePublic(key)

    override fun serializedPrivateKey(key: ExtendedKey): String = extendedKeySerializer.serializePrivate(key)

    override fun generateKey(): WalletDto {
        val seedPhrase = seedPhraseGenerator.createSeedPhrase(TWELVE)
        val rootExtendedKey = ExtendedKey.root(seedPhrase.toByteArray())

        val extendedKey = derivationKeysHelper.deriveDefaultAddress(rootExtendedKey)
        val addressKeyDto = AddressKeyDto(extendedKeySerializer.serializePublic(extendedKey),
                extendedKeySerializer.serializePrivate(extendedKey), extendedKey.ecKey.getAddress())

        return WalletDto(seedPhrase, extendedKeySerializer.serializePublic(rootExtendedKey),
                extendedKeySerializer.serializePrivate(rootExtendedKey), addressKeyDto)
    }

}
