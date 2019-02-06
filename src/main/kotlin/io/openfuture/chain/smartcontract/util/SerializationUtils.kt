package io.openfuture.chain.smartcontract.util

import java.io.*

object SerializationUtils {

    fun serialize(obj: Serializable): ByteArray {
        val out = ByteArrayOutputStream(512)
        ObjectOutputStream(out).writeObject(obj)
        return out.toByteArray()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> deserialize(bytes: ByteArray): T = ObjectInputStream(ByteArrayInputStream(bytes)).readObject() as T

}


