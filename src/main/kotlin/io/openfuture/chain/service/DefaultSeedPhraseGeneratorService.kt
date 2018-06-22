package io.openfuture.chain.service

import io.openfuture.chain.component.seed.PhraseLength.TWELVE
import io.openfuture.chain.component.seed.generator.SeedPhraseGenerator
import org.springframework.stereotype.Service

@Service
class DefaultSeedPhraseGeneratorService(
        private val seedPhraseGenerator: SeedPhraseGenerator
) : SeedPhraseGeneratorService {

    override fun generateSeedPhrase(): String = seedPhraseGenerator.createSeedPhrase(TWELVE)

}
