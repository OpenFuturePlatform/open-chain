package io.openfuture.chain.smartcontract.deploy.domain

import io.openfuture.chain.smartcontract.deploy.utils.asPackagePath
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.ASM6
import java.nio.file.Path


class ClassSource(
    val bytes: ByteArray
) {

    companion object {
        fun isClass(path: Path): Boolean = path.fileName.toString().endsWith(".class", true)
    }

    val reader = ClassReader(bytes)
    val writer = ClassWriter(reader, ASM6)

    /**
     * Fully qualified class name, e.g. io.openfuture.chain.HelloWorld
     */
    val qualifiedName
        get() = reader.className.asPackagePath


}
