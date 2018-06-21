package io.openfuture.chain.crypto.bip39

import io.openfuture.chain.crypto.bip39.dictionary.WordList
import io.openfuture.chain.util.HashUtils
import io.openfuture.chain.util.Sha256Utils
import org.springframework.stereotype.Component
import java.util.*

@Component
class Bip39MnemonicGenerator(private val wordList: WordList) {

    fun createMnemonic(entropy: ByteArray): String {
        var target = StringBuilder()
        val wordIndexes = wordIndexes(entropy)
        try {
            createMnemonic(wordIndexes, target)
        } finally {
            Arrays.fill(wordIndexes, 0)
        }
        return target.toString()
    }

    private fun createMnemonic(wordIndexes: IntArray, target: StringBuilder) {
        val separator = wordList.getSeparator().toString()
        for (i in wordIndexes.indices) {
            if (i > 0) {
                target.append(separator)
            }
            target.append(wordList.getWord(wordIndexes[i]))
        }
    }

    private fun wordIndexes(entropy: ByteArray): IntArray {
        val ent = entropy.size * 8
        entropyLengthPreChecks(ent)

        val entropyWithChecksum = Arrays.copyOf(entropy, entropy.size + 1)
        entropyWithChecksum[entropy.size] = firstByteOfSha256(entropy)

        val checksumLength = ent / 32

        val mnemonicLength = (ent + checksumLength) / 11

        val wordIndexes = IntArray(mnemonicLength)
        var bitOffset = 0
        var wordIndex = 0
        while (wordIndex < mnemonicLength) {
            wordIndexes[wordIndex] = HashUtils.next11Bits(entropyWithChecksum, bitOffset)
            bitOffset += 11
            wordIndex++
        }
        return wordIndexes
    }

    fun firstByteOfSha256(entropy: ByteArray): Byte {
        val hash = Sha256Utils.sha256(entropy)
        val firstByte = hash[0]
        Arrays.fill(hash, 0.toByte())
        return firstByte
    }

    private fun entropyLengthPreChecks(ent: Int) {
        if (ent < 128) {
            throw RuntimeException("Entropy too low, 128-256 bits allowed")
        }
        if (ent > 256) {
            throw RuntimeException("Entropy too high, 128-256 bits allowed")
        }
        if (ent % 32 > 0) {
            throw RuntimeException("Number of entropy bits must be divisible by 32")
        }
    }

}
