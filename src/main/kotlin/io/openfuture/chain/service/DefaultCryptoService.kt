package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.crypto.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.seed.PhraseLength.TWELVE
import io.openfuture.chain.crypto.seed.generator.SeedPhraseGenerator
import io.openfuture.chain.domain.crypto.key.KeyDto
import org.springframework.stereotype.Service

@Service
class DefaultCryptoService(
    private val seedPhraseGenerator: SeedPhraseGenerator,
    private val extendedKeySerializer: ExtendedKeySerializer
) : CryptoService {

    override fun generateSeedPhrase(): String = seedPhraseGenerator.createSeedPhrase(TWELVE)

    override fun generateKey(): KeyDto {
        val seedPhrase = seedPhraseGenerator.createSeedPhrase(TWELVE)

        val rootExtendedKey = ExtendedKey.root(seedPhrase.toByteArray())

        return KeyDto(extendedKeySerializer.serializePublic(rootExtendedKey),
                extendedKeySerializer.serializePrivate(rootExtendedKey))
    }

}
