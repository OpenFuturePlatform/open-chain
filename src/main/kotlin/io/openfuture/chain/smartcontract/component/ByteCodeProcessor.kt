package io.openfuture.chain.smartcontract.component

import io.openfuture.chain.smartcontract.util.asResourcePath
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.SimpleRemapper

object ByteCodeProcessor {

    fun getClassName(bytes: ByteArray): String = ClassReader(bytes).className

    fun renameClass(bytes: ByteArray, newName: String): ByteArray {
        val reader = ClassReader(bytes)
        val writer = ClassWriter(0)

        val oldName = reader.className.asResourcePath

        val adapter = ClassRemapper(writer, SimpleRemapper(oldName, newName))
        reader.accept(adapter, ClassReader.EXPAND_FRAMES)

        return writer.toByteArray()
    }

}