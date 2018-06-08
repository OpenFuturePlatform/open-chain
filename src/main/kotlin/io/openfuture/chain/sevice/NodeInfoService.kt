package io.openfuture.chain.sevice

import io.openfuture.chain.domain.NodeHardwareInfoResponse

interface NodeInfoService {

    fun getHardwareInfo(): NodeHardwareInfoResponse

}