package io.openfuture.chain.component.seed.calculator

interface PBKDF2WithHmacSHA256 {

    fun hash(chars: CharArray, salt: ByteArray): ByteArray

}