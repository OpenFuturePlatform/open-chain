package io.openfuture.chain.smartcontract.deploy.domain

import java.nio.file.Path

class ClassSource(val path: Path) {

    companion object {
        fun isClass(path: Path): Boolean = path.fileName.toString().endsWith(".class", true)
    }

    val name: String
        get() = path.fileName.toString().removeSuffix(".class")

}
