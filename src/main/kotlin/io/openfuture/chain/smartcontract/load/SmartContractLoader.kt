package io.openfuture.chain.smartcontract.load

import io.openfuture.chain.smartcontract.util.ByteCodeUtils
import io.openfuture.chain.smartcontract.util.asPackagePath

class SmartContractLoader : ClassLoader() {

    fun loadClass(bytes: ByteArray): Class<*> {
        val className = ByteCodeUtils.getClassName(bytes).asPackagePath
        return try {
            super.loadClass(className)
        } catch (e: ClassNotFoundException) {
            defineClass(className, bytes, 0, bytes.size)
        }
    }

}