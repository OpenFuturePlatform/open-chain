package io.openfuture.chain.service

import io.openfuture.chain.crypto.Words
import io.openfuture.chain.crypto.bip39.Bip39MnemonicGenerator
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class DefaultSeedPhraseGeneratorService(
        private val bip39MnemonicGenerator: Bip39MnemonicGenerator
) : SeedPhraseGeneratorService {

    override fun generateBip44SeedPhrase(): String {
        val wordCount = Words.TWELVE
        val secureRandom = SecureRandom()
        val entropy = ByteArray(wordCount.getByteLength())
        secureRandom.nextBytes(entropy)
        return bip39MnemonicGenerator.createMnemonic(entropy)
    }

}
