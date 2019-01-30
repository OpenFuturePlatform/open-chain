package io.openfuture.chain.smartcontract.util

import org.apache.commons.lang3.SerializationException
import java.io.*

object SerializationUtils {

    fun serialize(obj: Serializable): ByteArray {
        val out = ByteArrayOutputStream(512)
        try {
            ObjectOutputStream(out).writeObject(obj)
        } catch (ex: IOException) {
            throw SerializationException(ex.message)
        }
        return out.toByteArray()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> deserialize(bytes: ByteArray): T {
        try {
            return ObjectInputStream(ByteArrayInputStream(bytes)).readObject() as T
        } catch (ex: IOException) {
            throw SerializationException(ex.message)
        }
    }

}


