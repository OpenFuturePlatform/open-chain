package io.openfuture.chain.service

import io.openfuture.chain.crypto.Words
import io.openfuture.chain.crypto.bip44.Bip44MnemonicGenerator
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class DefaultSeedPhraseGeneratorService(
        private val bip44MnemonicGenerator: Bip44MnemonicGenerator
) : SeedPhraseGeneratorService {

    override fun generateBip44SeedPhrase(): String {
        val wordCount = Words.TWELVE
        val secureRandom = SecureRandom()
        val entropy = ByteArray(wordCount.getByteLength())
        secureRandom.nextBytes(entropy)
        return bip44MnemonicGenerator.createMnemonic(entropy)
    }

}
