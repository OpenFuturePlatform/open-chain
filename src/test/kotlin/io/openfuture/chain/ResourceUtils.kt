package io.openfuture.chain

import java.io.File

object ResourceUtils {

    fun getResource(path: String): File = File(javaClass.getResource(path).toURI())

    fun getResourceBytes(path: String): ByteArray = getResource(path).readBytes()

}