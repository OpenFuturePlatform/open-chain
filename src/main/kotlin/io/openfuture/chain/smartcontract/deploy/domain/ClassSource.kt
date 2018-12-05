package io.openfuture.chain.smartcontract.deploy.domain

import io.openfuture.chain.smartcontract.deploy.utils.asPackagePath
import org.objectweb.asm.ClassReader
import java.nio.file.Path


class ClassSource(
    val bytes: ByteArray
) {

    companion object {
        fun isClass(path: Path): Boolean = path.fileName.toString().endsWith(".class", true)
    }

    val reader = ClassReader(bytes)

    /**
     * Fully qualified class name, e.g. io.openfuture.chain.HelloWorld
     */
    val qualifiedName
        get() = reader.className.asPackagePath


}
