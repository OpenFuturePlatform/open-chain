package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.key.ExtendedKeyDeserializer
import io.openfuture.chain.crypto.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.key.PrivateKeyManager
import io.openfuture.chain.crypto.seed.PhraseLength.TWELVE
import io.openfuture.chain.crypto.seed.generator.SeedPhraseGenerator
import io.openfuture.chain.domain.crypto.key.AddressKeyDto
import org.springframework.stereotype.Service

@Service
class DefaultCryptoService(
    private val seedPhraseGenerator: SeedPhraseGenerator,
    private val keyManager: PrivateKeyManager,
    private val serializer: ExtendedKeySerializer,
    private val deserializer: ExtendedKeyDeserializer
) : CryptoService {

    override fun importKey(key: String): AddressKeyDto {
        val extendedKey = deserializer.deserialize(key)
        return AddressKeyDto(
            serializer.serializePublic(extendedKey),
            if (!extendedKey.ecKey.isPrivateEmpty()) serializer.serializePrivate(extendedKey) else null,
            extendedKey.ecKey.getAddress()
        )
    }

    override fun importWifKey(wifKey: String): ECKey = keyManager.importPrivateKey(wifKey)

    override fun generateSeedPhrase(): String = seedPhraseGenerator.createSeedPhrase(TWELVE)

}
