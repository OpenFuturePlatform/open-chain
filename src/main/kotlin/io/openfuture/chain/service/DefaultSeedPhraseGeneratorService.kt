package io.openfuture.chain.service

import io.openfuture.chain.crypto.Words
import io.openfuture.chain.crypto.seed.generator.SeedPhraseGenerator
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class DefaultSeedPhraseGeneratorService(
        private val seedPhraseGenerator: SeedPhraseGenerator
) : SeedPhraseGeneratorService {

    override fun generateSeedPhrase(): String {
        val wordCount = Words.TWELVE
        val secureRandom = SecureRandom()
        val entropy = ByteArray(wordCount.getByteLength())
        secureRandom.nextBytes(entropy)
        return seedPhraseGenerator.createSeedPhrase(entropy)
    }

}
