package io.openfuture.chain.crypto.service

import io.openfuture.chain.crypto.component.key.DerivationKeysHelper
import io.openfuture.chain.crypto.component.key.ExtendedKeyDeserializer
import io.openfuture.chain.crypto.component.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.component.key.PrivateKeyManager
import io.openfuture.chain.crypto.component.seed.SeedCalculator
import io.openfuture.chain.crypto.component.seed.SeedPhraseGenerator
import io.openfuture.chain.crypto.model.dictionary.PhraseLength
import io.openfuture.chain.crypto.model.dto.ECKey
import io.openfuture.chain.crypto.model.dto.ExtendedKey
import io.openfuture.chain.crypto.validation.SeedPhraseValidator
import io.openfuture.chain.rpc.domain.crypto.AccountDto
import io.openfuture.chain.rpc.domain.crypto.WalletDto
import io.openfuture.chain.rpc.domain.crypto.key.KeyDto
import org.springframework.stereotype.Service

@Service
class DefaultCryptoService(
    private val seedPhraseGenerator: SeedPhraseGenerator,
    private val keyManager: PrivateKeyManager,
    private val serializer: ExtendedKeySerializer,
    private val deserializer: ExtendedKeyDeserializer,
    private val derivationKeysHelper: DerivationKeysHelper,
    private val seedPhraseValidator: SeedPhraseValidator,
    private val seedCalculator: SeedCalculator
) : CryptoService {

    override fun generateSeedPhrase(): String = seedPhraseGenerator.createSeedPhrase(PhraseLength.TWELVE)

    override fun generateNewAccount(): AccountDto = getRootAccount(generateSeedPhrase())

    override fun getRootAccount(seedPhrase: String): AccountDto {
        if (!seedPhraseValidator.isValid(seedPhrase))
            throw IllegalArgumentException("Invalid seed phrase")

        val rootExtendedKey = ExtendedKey.root(seedCalculator.calculateSeed(seedPhrase))
        val extendedKey = derivationKeysHelper.deriveDefaultAddress(rootExtendedKey)

        return AccountDto(
            seedPhrase,
            KeyDto(serializer.serializePublic(rootExtendedKey), serializer.serializePrivate(rootExtendedKey)),
            WalletDto(
                KeyDto(serializer.serializePublic(extendedKey), serializer.serializePrivate(extendedKey)),
                extendedKey.ecKey.getAddress()
            )
        )
    }

    override fun getDerivationKey(seedPhrase: String, derivationPath: String): ExtendedKey {
        if (!seedPhraseValidator.isValid(seedPhrase))
            throw IllegalArgumentException("Invalid seed phrase")

        val masterKey = ExtendedKey.root(seedCalculator.calculateSeed(seedPhrase))
        return derivationKeysHelper.derive(masterKey, derivationPath)
    }

    override fun importKey(key: String): ExtendedKey = deserializer.deserialize(key)

    override fun importWifKey(wifKey: String): ECKey = keyManager.importPrivateKey(wifKey)

    override fun serializePublicKey(key: ExtendedKey): String = serializer.serializePublic(key)

    override fun serializePrivateKey(key: ExtendedKey): String = serializer.serializePrivate(key)

}
