package io.openfuture.chain.crypto.key

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.service.CryptoService
import org.springframework.stereotype.Component
import java.io.File
import java.nio.charset.Charset
import javax.annotation.PostConstruct

@Component
class KeyHolder(
    private val properties: NodeProperties,
    private val cryptoService: CryptoService
) {

    private var privateKey: ByteArray? = null

    private var publicKey: ByteArray? = null


    @PostConstruct
    private fun init() {
        generatePrivatePublicKeysIfNotExist()

        val privateKeyValue = File(properties.privateKeyPath).readText(Charset.forName("UTF-8"))
        val publicKeyValue = File(properties.publicKeyPath).readText(Charset.forName("UTF-8"))

        privateKey = HashUtils.hexStringToBytes(privateKeyValue)
        publicKey = HashUtils.hexStringToBytes(publicKeyValue)
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
            val rootExtendedKey = cryptoService.getMasterKey(seedPhrase)
            val privateKeyValue = HashUtils.bytesToHexString(rootExtendedKey.ecKey.private!!.toByteArray())
            val publicKeyValue = HashUtils.bytesToHexString(rootExtendedKey.ecKey.public)

            privateKeyFile.writeText(privateKeyValue, Charset.forName("UTF-8"))
            publicKeyFile.writeText(publicKeyValue, Charset.forName("UTF-8"))
        }
    }

}