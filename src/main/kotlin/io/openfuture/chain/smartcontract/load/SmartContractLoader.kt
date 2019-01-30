package io.openfuture.chain.smartcontract.load

import io.openfuture.chain.smartcontract.util.ByteCodeUtils

class SmartContractLoader : ClassLoader() {

    fun loadClass(bytes: ByteArray): Class<*> {
        val className = ByteCodeUtils.getClassName(bytes)
        return try {
            super.loadClass(className)
        } catch (e: ClassNotFoundException) {
            defineClass(className, bytes, 0, bytes.size)
        }
    }

}