package io.openfuture.chain.core.component

import io.openfuture.chain.crypto.model.dto.ECKey
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class NodeKeyHolder(
    private val config: NodeConfigurator,
    private val cryptoService: CryptoService
) {

    private var privateKey: ByteArray? = null
    private var publicKey: ByteArray? = null


    @PostConstruct
    private fun init() {
        generateKeysIfNotExist()

        privateKey = ByteUtils.fromHexString(config.getConfig().secret)
        publicKey = ECKey(privateKey!!, true).public
    }

    fun getPrivateKey(): ByteArray = privateKey!!

    fun getPublicKey(): String = ByteUtils.toHexString(publicKey!!)

    fun getUid(key: ByteArray = publicKey!!): String = ByteUtils.toHexString(HashUtils.sha256(key))

    private fun generateKeysIfNotExist() {
        if (config.getConfig().secret.isEmpty()) {
            val seedPhrase = cryptoService.generateSeedPhrase()
            val masterKey = cryptoService.getMasterKey(seedPhrase).ecKey

            config.getConfig().secret = ByteUtils.toHexString(masterKey.getPrivate())
        }
    }

}