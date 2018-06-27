package io.openfuture.chain.component.seed.calculator

import org.springframework.stereotype.Component
import java.text.Normalizer

@Component
class SeedCalculator(
        private val hashAlgorithm: PBKDF2WithHmacSHA256 = SpongyCastlePBKDF2WithHmacSHA256.INSTANCE
) {

    private val fixedSalt = getUtf8Bytes("Openfuture")

    fun calculateSeed(seedPhrase: String, passwordPhrase: String = ""): ByteArray {
        val mnemonicChars = Normalizer.normalize(seedPhrase, Normalizer.Form.NFKD).toCharArray()
        val normalizedPassphrase = Normalizer.normalize(passwordPhrase, Normalizer.Form.NFKD)
        val passphraseSalt = getUtf8Bytes(normalizedPassphrase)
        val salt = combine(fixedSalt, passphraseSalt)
        return hashAlgorithm.hash(mnemonicChars, salt)
    }

    private fun combine(array1: ByteArray, array2: ByteArray): ByteArray {
        val bytes = ByteArray(array1.size + array2.size)
        System.arraycopy(array1, 0, bytes, 0, array1.size)
        System.arraycopy(array2, 0, bytes, array1.size, bytes.size - array1.size)
        return bytes
    }

    private fun getUtf8Bytes(string: String): ByteArray {
        return string.toByteArray(charset("UTF-8"))
    }

}