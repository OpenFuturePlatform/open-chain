package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.crypto.key.DerivationKeysHelper
import io.openfuture.chain.crypto.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.seed.PhraseLength.TWELVE
import io.openfuture.chain.crypto.seed.calculator.SeedCalculator
import io.openfuture.chain.crypto.seed.generator.SeedPhraseGenerator
import io.openfuture.chain.domain.crypto.key.AddressKeyDto
import io.openfuture.chain.domain.crypto.key.KeyDto
import org.springframework.stereotype.Service

@Service
class DefaultCryptoService(
    private val seedPhraseGenerator: SeedPhraseGenerator,
    private val seedCalculator: SeedCalculator,
    private val derivationKeyHelper: DerivationKeysHelper,
    private val extendedKeySerializer: ExtendedKeySerializer
) : CryptoService {

    override fun generateSeedPhrase(): String = seedPhraseGenerator.createSeedPhrase(TWELVE)

    override fun getMasterKey(seedPhrase: String): KeyDto {
        val seed = seedCalculator.calculateSeed(seedPhrase)
        val masterKey = ExtendedKey.root(seed)

        return KeyDto(
                extendedKeySerializer.serializePublic(masterKey),
                extendedKeySerializer.serializePrivate(masterKey)
        )
    }

    override fun getDerivationKey(seedPhrase: String, derivationPath: String): AddressKeyDto {
        val seed = seedCalculator.calculateSeed(seedPhrase)
        val masterKey = ExtendedKey.root(seed)
        val derivationKey = derivationKeyHelper.derive(masterKey, derivationPath)

        return AddressKeyDto(
                extendedKeySerializer.serializePublic(derivationKey),
                extendedKeySerializer.serializePrivate(derivationKey),
                derivationKey.ecKey.getAddress()
        )
    }

}
