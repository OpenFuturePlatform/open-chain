package io.openfuture.chain.core.component

import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.network.property.NodeProperties
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PostConstruct
import kotlin.text.Charsets.UTF_8

@Component
class NodeKeyHolder(
    private val properties: NodeProperties,
    private val cryptoService: CryptoService
) {

    private var privateKey: ByteArray? = null
    private var publicKey: ByteArray? = null


    @PostConstruct
    private fun init() {
        generatePrivatePublicKeysIfNotExist()

        privateKey = ByteUtils.fromHexString(File(properties.privateKeyPath).readText(UTF_8))
        publicKey = ByteUtils.fromHexString(File(properties.publicKeyPath).readText(UTF_8))
    }

    fun getPrivateKey(): ByteArray = privateKey!!

    fun getPublicKey(): String = ByteUtils.toHexString(publicKey!!)

    fun getUid(key: ByteArray = publicKey!!): String = ByteUtils.toHexString(HashUtils.sha256(key))

    private fun generatePrivatePublicKeysIfNotExist() {
        val privateKeyFile = File(properties.privateKeyPath)
        val publicKeyFile = File(properties.publicKeyPath)

        if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
            val seedPhrase = cryptoService.generateSeedPhrase()
            val masterKey = cryptoService.getMasterKey(seedPhrase).ecKey

            privateKeyFile.writeText(ByteUtils.toHexString(masterKey.getPrivate()), UTF_8)
            publicKeyFile.writeText(ByteUtils.toHexString(masterKey.public), UTF_8)
        }
    }

}