package io.openfuture.chain.crypto.signature

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Component

@Component
class SignatureManager {

    // will be removed when will be implemented by another developer
    fun sign(data: ByteArray, privateKey: ByteArray): String =
        RandomStringUtils.randomAlphabetic(16)

    // will be removed when will be implemented by another developer
    fun verify(data: ByteArray, signature: String, publicKey: ByteArray) = true

}