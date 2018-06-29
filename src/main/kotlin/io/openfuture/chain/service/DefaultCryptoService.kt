package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.crypto.key.DerivationKeysHelper
import io.openfuture.chain.crypto.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.seed.PhraseLength.TWELVE
import io.openfuture.chain.crypto.seed.generator.SeedPhraseGenerator
import io.openfuture.chain.domain.crypto.key.AddressKeyDto
import io.openfuture.chain.domain.crypto.key.WalletDto
import org.springframework.stereotype.Service

@Service
class DefaultCryptoService(
        private val seedPhraseGenerator: SeedPhraseGenerator,
        private val extendedKeySerializer: ExtendedKeySerializer,
        private val derivationKeysHelper: DerivationKeysHelper
) : CryptoService {

    companion object {
        private const val DEFAULT_DERIVATION_PATH = "m/0/0/0"
    }

    override fun generateSeedPhrase(): String = seedPhraseGenerator.createSeedPhrase(TWELVE)

    override fun generateKey(): WalletDto {
        val seedPhrase = seedPhraseGenerator.createSeedPhrase(TWELVE)
        val rootExtendedKey = ExtendedKey.root(seedPhrase.toByteArray())

        val extendedKey = derivationKeysHelper.derive(rootExtendedKey, DEFAULT_DERIVATION_PATH)
        val addressKeyDto = AddressKeyDto(extendedKeySerializer.serializePublic(extendedKey),
                extendedKeySerializer.serializePrivate(extendedKey), extendedKey.ecKey.getAddress())

        return WalletDto(extendedKeySerializer.serializePublic(rootExtendedKey),
                extendedKeySerializer.serializePrivate(rootExtendedKey), seedPhrase, addressKeyDto)
    }

}
