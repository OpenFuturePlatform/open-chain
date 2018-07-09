package io.openfuture.chain.crypto.signature

import org.springframework.stereotype.Component

@Component
class SignatureManager {

    // will be removed when will be implemented by another developer
    fun sign(data: ByteArray, privateKey: ByteArray): String =
        "21334324324235435454565656565656546356fsdafdasfdasfd324134324432"

    // will be removed when will be implemented by another developer
    fun verify(data: ByteArray, signature: String, publicKey: ByteArray) = true

}