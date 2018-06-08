package io.openfuture.chain.domain.hardware

data class RamInfoDto(val type: String, val free: Long, val used: Long, val total: Long)