package io.openfuture.chain.crypto.component.key

import io.openfuture.chain.crypto.model.dto.ECKey
import io.openfuture.chain.crypto.model.dto.ExtendedKey
import io.openfuture.chain.crypto.util.Base58
import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Arrays.areEqual
import org.springframework.stereotype.Component

/**
 * Component for deserialization public and private serialized keys to ExtendedKey entity.
 * From the serialized private key value, the public and private keys will be deserialized.
 * And from the serialized public key value, will be deserialized only public key.
 */
@Component
class ExtendedKeyDeserializer {

    companion object {
        private const val DECODED_SERIALIZED_KEY_LENGTH = 78

        private const val KEY_TYPE_LENGTH = 4
        private const val DEPTH_LENGTH = 1
        private const val FINGERPRINT_LENGTH = 4
        private const val SEQUENCE_LENGTH = 4
        private const val CHAIN_CODE_LENGTH = 32

        private const val DEPTH_INDEX = KEY_TYPE_LENGTH
        private const val FINGERPRINT_INDEX = DEPTH_INDEX + DEPTH_LENGTH
        private const val SEQUENCE_INDEX = FINGERPRINT_INDEX + FINGERPRINT_LENGTH
        private const val CHAIN_CODE_INDEX = SEQUENCE_INDEX + SEQUENCE_LENGTH
        private const val KEY_BYTES_INDEX = CHAIN_CODE_INDEX + CHAIN_CODE_LENGTH
    }


    fun deserialize(serializedKey: String): ExtendedKey {
        val decodedSerializedKey = Base58.decodeWithChecksum(serializedKey)

        if (DECODED_SERIALIZED_KEY_LENGTH != decodedSerializedKey.size) {
            throw IllegalArgumentException("Invalid serialized key value")
        }

        val depth = intFromByte(decodedSerializedKey[DEPTH_INDEX])
        val parentFingerprint = byteArrayToInt(
            Arrays.copyOfRange(decodedSerializedKey, FINGERPRINT_INDEX, SEQUENCE_INDEX)
        )
        val sequence = byteArrayToInt(
            Arrays.copyOfRange(decodedSerializedKey, SEQUENCE_INDEX, CHAIN_CODE_INDEX)
        )
        val chainCode = Arrays.copyOfRange(decodedSerializedKey, CHAIN_CODE_INDEX, KEY_BYTES_INDEX)
        val keyBytes = Arrays.copyOfRange(decodedSerializedKey, KEY_BYTES_INDEX, decodedSerializedKey.size)
        val ecKey = ECKey(keyBytes, isSerializedKeyPrivate(decodedSerializedKey))

        return ExtendedKey(
            keyHash = ByteArray(64),
            ecKey = ecKey,
            chainCode = chainCode,
            sequence = sequence,
            depth = depth,
            parentFingerprint = parentFingerprint
        )
    }

    private fun isSerializedKeyPrivate(decodedSerializedKey: ByteArray): Boolean {
        val keyType = Arrays.copyOf(decodedSerializedKey, KEY_TYPE_LENGTH)

        return when {
            areEqual(keyType, ExtendedKeySerializer.xprv) -> true
            areEqual(keyType, ExtendedKeySerializer.xpub) -> false
            else -> throw Exception("Invalid key type")
        }
    }

    private fun intFromByte(byte: Byte): Int = (byte.toInt() and 0xff)

    private fun byteArrayToInt(bytes: ByteArray): Int =
        intFromByte(bytes[3])
            .or(intFromByte(bytes[2]) shl 8)
            .or(intFromByte(bytes[1]) shl 16)
            .or(intFromByte(bytes[0]) shl 24)

}