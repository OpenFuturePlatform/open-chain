package io.openfuture.chain.crypto.key

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.crypto.util.Base58CoderUtils
import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Arrays.areEqual
import org.springframework.stereotype.Component
import kotlin.experimental.and

/**
 * Component for deserialization public and private serialized keys to ExtendedKey entity.
 * From the serialized private key value, the public and private keys will be deserialized.
 * And from the serialized public key value, will be deserialized only public key.
 */
@Component
class ExtendedKeyDeserializer {

    companion object {
        private const val DECODED_SERIALIZED_KEY_LENGTH = 78
    }

    fun deserialize(serializedKey: String): ExtendedKey {
        val decodedSerializedKey = Base58CoderUtils.decodeWithChecksum(serializedKey)

        if (DECODED_SERIALIZED_KEY_LENGTH != decodedSerializedKey.size) {
            throw IllegalArgumentException("Invalid serialized key value")
        }

        val depth = toIntFromByte(decodedSerializedKey[4])

        var parentFingerprint = toIntFromByte(decodedSerializedKey[5])
        parentFingerprint = parentFingerprint shl 8
        parentFingerprint = parentFingerprint or toIntFromByte(decodedSerializedKey[6])
        parentFingerprint = parentFingerprint shl 8
        parentFingerprint = parentFingerprint or toIntFromByte(decodedSerializedKey[7])
        parentFingerprint = parentFingerprint shl 8
        parentFingerprint = parentFingerprint or toIntFromByte(decodedSerializedKey[8])

        var sequence = toIntFromByte(decodedSerializedKey[9])
        sequence = sequence shl 8
        sequence = sequence or toIntFromByte(decodedSerializedKey[10])
        sequence = sequence shl 8
        sequence = sequence or toIntFromByte(decodedSerializedKey[11])
        sequence = sequence shl 8
        sequence = sequence or toIntFromByte(decodedSerializedKey[12])

        val chainCode = Arrays.copyOfRange(decodedSerializedKey, 13, 13 + 32)
        val keyBytes = Arrays.copyOfRange(decodedSerializedKey, 13 + 32, decodedSerializedKey.size)
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
        val keyType = Arrays.copyOf(decodedSerializedKey, 4)

        return when {
            areEqual(keyType, ExtendedKeySerializer.xprv) -> true
            areEqual(keyType, ExtendedKeySerializer.xpub) -> false
            else -> throw Exception("Invalid or unsupported key type")
        }
    }

    private fun toIntFromByte(byte: Byte): Int = (byte and 0xff.toByte()).toInt()

}