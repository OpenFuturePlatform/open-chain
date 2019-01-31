package io.openfuture.chain.smartcontract.component.load

import io.openfuture.chain.smartcontract.component.ByteCodeProcessor
import io.openfuture.chain.smartcontract.util.asPackagePath

class SmartContractLoader : ClassLoader() {

    fun loadClass(bytes: ByteArray): Class<*> {
        val className = ByteCodeProcessor.getClassName(bytes).asPackagePath
        return try {
            super.loadClass(className)
        } catch (e: ClassNotFoundException) {
            defineClass(className, bytes, 0, bytes.size)
        }
    }

}