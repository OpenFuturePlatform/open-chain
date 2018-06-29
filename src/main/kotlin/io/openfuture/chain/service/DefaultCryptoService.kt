package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.crypto.seed.PhraseLength.TWELVE
import io.openfuture.chain.crypto.seed.generator.SeedPhraseGenerator
import org.springframework.stereotype.Service

@Service
class DefaultCryptoService(
    private val seedPhraseGenerator: SeedPhraseGenerator
) : CryptoService {

    override fun generateSeedPhrase(): String = seedPhraseGenerator.createSeedPhrase(TWELVE)

    override fun generateKey(): String {
        val seedPhrase = seedPhraseGenerator.createSeedPhrase(TWELVE)

        val rootExtendedKey = ExtendedKey.root(seedPhrase.toByteArray())

        return rootExtendedKey;
    }

}
