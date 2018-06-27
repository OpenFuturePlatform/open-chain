package io.openfuture.chain.crypto.seed.calculator

interface PBKDF2WithHmacSHA256 {

    fun hash(chars: CharArray, salt: ByteArray): ByteArray

}