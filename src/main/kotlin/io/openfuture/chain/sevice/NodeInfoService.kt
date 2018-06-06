package io.openfuture.chain.sevice

import io.openfuture.chain.domain.NodeHardwareResponse

interface NodeInfoService {

    fun getHardwareInfo(): NodeHardwareResponse

}