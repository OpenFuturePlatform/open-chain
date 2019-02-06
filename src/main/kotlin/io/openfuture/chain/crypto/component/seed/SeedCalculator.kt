package io.openfuture.chain.crypto.component.seed

import io.openfuture.chain.crypto.constants.SeedConstant
import io.openfuture.chain.crypto.util.HashUtils
import org.springframework.stereotype.Component
import java.text.Normalizer

@Component
class SeedCalculator {

    fun calculateSeed(seedPhrase: String, passwordPhrase: String = ""): ByteArray {
        val mnemonicChars = Normalizer.normalize(seedPhrase, Normalizer.Form.NFKD).toCharArray()
        val normalizedPassphrase = Normalizer.normalize(passwordPhrase, Normalizer.Form.NFKD)
        val passphraseSalt = getUtf8Bytes(normalizedPassphrase)
        val salt = combine(getUtf8Bytes(SeedConstant.SALT), passphraseSalt)
        return HashUtils.pbkdf2(mnemonicChars, salt)
    }

    private fun combine(array1: ByteArray, array2: ByteArray): ByteArray {
        val bytes = ByteArray(array1.size + array2.size)
        System.arraycopy(array1, 0, bytes, 0, array1.size)
        System.arraycopy(array2, 0, bytes, array1.size, bytes.size - array1.size)
        return bytes
    }

    private fun getUtf8Bytes(string: String): ByteArray = string.toByteArray()

}