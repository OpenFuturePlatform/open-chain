package io.openfuture.chain.crypto.seed.calculator

import io.openfuture.chain.crypto.util.HashUtils
import org.springframework.stereotype.Component
import java.text.Normalizer

@Component
class SeedCalculator {

    private val fixedSalt = getUtf8Bytes("Openfuture")


    fun calculateSeed(seedPhrase: String, passwordPhrase: String = ""): ByteArray {
        val mnemonicChars = Normalizer.normalize(seedPhrase, Normalizer.Form.NFKD).toCharArray()
        val normalizedPassphrase = Normalizer.normalize(passwordPhrase, Normalizer.Form.NFKD)
        val passphraseSalt = getUtf8Bytes(normalizedPassphrase)
        val salt = combine(fixedSalt, passphraseSalt)
        return HashUtils.hashPBKDF2(mnemonicChars, salt)
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