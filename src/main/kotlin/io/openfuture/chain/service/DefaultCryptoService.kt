package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.crypto.key.DerivationKeysHelper
import io.openfuture.chain.crypto.seed.PhraseLength.TWELVE
import io.openfuture.chain.crypto.seed.calculator.SeedCalculator
import io.openfuture.chain.crypto.seed.generator.SeedPhraseGenerator
import org.springframework.stereotype.Service

@Service
class DefaultCryptoService(
    private val seedPhraseGenerator: SeedPhraseGenerator,
    private val seedCalculator: SeedCalculator,
    private val derivationKeyHelper: DerivationKeysHelper
) : CryptoService {

    override fun generateSeedPhrase(): String = seedPhraseGenerator.createSeedPhrase(TWELVE)

    override fun getMasterKey(seedPhrase: String): ExtendedKey {
        val seed = seedCalculator.calculateSeed(seedPhrase)
        return ExtendedKey.root(seed)
    }

    override fun getDerivationKey(seedPhrase: String, derivationPath: String): ExtendedKey {
        val masterKey = getMasterKey(seedPhrase)
        return derivationKeyHelper.derive(masterKey, derivationPath)
    }

}
