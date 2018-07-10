package io.openfuture.chain.block

data class SignaturePublicKeyPair(

    val signature: String,

    val publicKey: String

) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SignaturePublicKeyPair

        if (signature != other.signature) return false
        if (publicKey != other.publicKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = signature.hashCode()
        result = 31 * result + publicKey.hashCode()
        return result
    }

}