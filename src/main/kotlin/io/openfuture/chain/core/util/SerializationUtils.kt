package io.openfuture.chain.core.util

import org.hibernate.internal.util.SerializationHelper
import java.io.*

object SerializationUtils {

    fun serialize(obj: Serializable): ByteArray {
        val out = ByteArrayOutputStream(512)
        ObjectOutputStream(out).writeObject(obj)
        return out.toByteArray()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> deserialize(bytes: ByteArray, classLoader: ClassLoader): T =
        SerializationHelper.deserialize(bytes, classLoader) as T

    fun deserialize(bytes: ByteArray): Serializable = ObjectInputStream(ByteArrayInputStream(bytes)).readObject() as Serializable

}


