package io.openfuture.chain.domain.hardware

data class NetworkInfoDto(
        val localIp: List<String>,
        val remoteIp: List<String>,
        val networkInterfaces: List<NetworkInterfaceInfoDto>
)