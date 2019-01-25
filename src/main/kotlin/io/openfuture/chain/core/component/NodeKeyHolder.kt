package io.openfuture.chain.core.component

import io.openfuture.chain.crypto.model.dto.ECKey
import io.openfuture.chain.crypto.service.CryptoService
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class NodeKeyHolder(
    private val config: NodeConfigurator,
    private val cryptoService: CryptoService
) {

    private lateinit var privateKey: ByteArray
    private lateinit var publicKey: ByteArray


    @PostConstruct
    private fun init() {
        generateKeysIfNotExist()

        privateKey = ByteUtils.fromHexString(config.getConfig().secret)
        publicKey = ECKey(privateKey, true).public
    }

    fun getPrivateKey(): ByteArray = privateKey

    fun getPublicKeyAsHexString(): String = ByteUtils.toHexString(publicKey)

    private fun generateKeysIfNotExist() {
        if (config.getConfig().secret.isEmpty()) {
            val seedPhrase = cryptoService.generateSeedPhrase()
            val masterKey = cryptoService.getMasterKey(seedPhrase).ecKey

            config.setSecret(ByteUtils.toHexString(masterKey.getPrivate()))
        }
    }

}