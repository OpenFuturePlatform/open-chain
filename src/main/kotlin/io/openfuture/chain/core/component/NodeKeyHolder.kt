package io.openfuture.chain.core.component

import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.network.property.NodeProperties
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PostConstruct

@Component
class NodeKeyHolder(
    private val properties: NodeProperties,
    private val cryptoService: CryptoService
) {

    private var privateKey: ByteArray? = null
    private var publicKey: String? = null


    @PostConstruct
    private fun init() {
        generatePrivatePublicKeysIfNotExist()

        privateKey = ByteUtils.fromHexString(File(properties.privateKeyPath).readText(Charsets.UTF_8))
        publicKey = File(properties.publicKeyPath).readText(Charsets.UTF_8)
    }

    fun getPrivateKey(): ByteArray {
        return privateKey!!
    }

    fun getPublicKey(): String {
        return publicKey!!
    }

    private fun generatePrivatePublicKeysIfNotExist() {
        val privateKeyFile = File(properties.privateKeyPath)
        val publicKeyFile = File(properties.publicKeyPath)

        if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
            val seedPhrase = cryptoService.generateSeedPhrase()
            val masterKey = cryptoService.getMasterKey(seedPhrase).ecKey

            privateKeyFile.writeText(ByteUtils.toHexString(masterKey.getPrivate()), Charsets.UTF_8)
            publicKeyFile.writeText(ByteUtils.toHexString(masterKey.public), Charsets.UTF_8)
        }
    }

}