package io.openfuture.chain.sevice

import io.openfuture.chain.domain.NodeHardwareResponse

interface HardwareInfoService {

    fun getHardwareInfo(): NodeHardwareResponse

}