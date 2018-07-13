package io.openfuture.chain.crypto.key

import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.crypto.seed.calculator.SeedCalculator
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.service.CryptoService
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Component
import java.io.File
import java.nio.charset.Charset
import javax.annotation.PostConstruct

@Component
class NodeKeyHolder(
    private val properties: NodeProperties,
    private val cryptoService: CryptoService,
    private val seedCalculator: SeedCalculator
) {

    private var privateKey: ByteArray? = null
    private var publicKey: ByteArray? = null


    @PostConstruct
    private fun init() {
        generatePrivatePublicKeysIfNotExist()

        val privateKeyValue = File(properties.privateKeyPath).readText(Charset.forName("UTF-8"))
        val publicKeyValue = File(properties.publicKeyPath).readText(Charset.forName("UTF-8"))

        privateKey = ByteUtils.fromHexString(privateKeyValue)
        publicKey = ByteUtils.fromHexString(publicKeyValue)
    }

    fun getPrivateKey(): ByteArray {
        return privateKey!!
    }

    fun getPublicKey(): ByteArray {
        return publicKey!!
    }

    private fun generatePrivatePublicKeysIfNotExist() {
        val privateKeyFile = File(properties.privateKeyPath)
        val publicKeyFile = File(properties.publicKeyPath)

        if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
            val seedPhrase = cryptoService.generateSeedPhrase()
            val masterKey = ExtendedKey.root(seedCalculator.calculateSeed(seedPhrase))
            val privateKeyValue = ByteUtils.toHexString(masterKey.ecKey.getPrivate())
            val publicKeyValue = ByteUtils.toHexString(masterKey.ecKey.public)

            privateKeyFile.writeText(privateKeyValue, Charsets.UTF_8)
            publicKeyFile.writeText(publicKeyValue, Charsets.UTF_8)
        }
    }

}