package io.openfuture.chain.domain.rpc.base

import io.openfuture.chain.crypto.util.HashUtils

abstract class BaseRequest() {

    abstract fun getBytes(): ByteArray

    fun getHash(): String = HashUtils.toHexString(HashUtils.sha256(getBytes()))

}