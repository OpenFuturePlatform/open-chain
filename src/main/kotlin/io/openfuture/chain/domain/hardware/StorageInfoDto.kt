package io.openfuture.chain.domain.hardware

data class StorageInfoDto(
        val type: String,
        val freeStorage: Long,
        val usableStorage: Long,
        val totalStorage: Long
)