package io.openfuture.chain.smartcontract.deploy.utils

import org.apache.commons.lang3.SerializationException
import java.io.*

object SerializationUtils {

    fun serialize(obj: Serializable): ByteArray {
        val out = ByteArrayOutputStream(512)
        serialize(obj, out)
        return out.toByteArray()
    }

    private fun serialize(obj: Serializable, outputStream: OutputStream) {
        try {
            ObjectOutputStream(outputStream).writeObject(obj)
        } catch (ex: IOException) {
            throw SerializationException(ex.message)
        }
    }

    fun <T> deserialize(objectData: ByteArray, classLoader: ClassLoader): T =
        deserialize(ByteArrayInputStream(objectData), classLoader)

    @Suppress("UNCHECKED_CAST")
    private fun <T> deserialize(inputStream: InputStream, classLoader: ClassLoader): T {
        try {
            return ClassLoaderAwareObjectInputStream(inputStream, classLoader).readObject() as T
        } catch (ex: ClassNotFoundException) {
            throw SerializationException(ex.message)
        } catch (ex: IOException) {
            throw SerializationException(ex.message)
        }
    }

    internal class ClassLoaderAwareObjectInputStream(
        inStream: InputStream,
        private val classLoader: ClassLoader
    ) : ObjectInputStream(inStream) {

        override fun resolveClass(desc: ObjectStreamClass): Class<*> = try {
            Class.forName(desc.name, false, classLoader)
        } catch (ex: ClassNotFoundException) {
            Class.forName(desc.name, false, Thread.currentThread().contextClassLoader)
        }

    }

}


