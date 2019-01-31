package io.openfuture.chain.smartcontract.util

import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.commons.RemappingClassAdapter
import jdk.internal.org.objectweb.asm.commons.SimpleRemapper

object ByteCodeUtils {

    fun getClassName(bytes: ByteArray): String = ClassReader(bytes).className

    fun processByteArray(bytes: ByteArray, newName: String): ByteArray {
        val reader = ClassReader(bytes)
        val writer = ClassWriter(0)

        val oldName = reader.className.asResourcePath

        val adapter = RemappingClassAdapter(writer, SimpleRemapper(oldName, newName))
        reader.accept(adapter, ClassReader.EXPAND_FRAMES)

        return writer.toByteArray()
    }

}