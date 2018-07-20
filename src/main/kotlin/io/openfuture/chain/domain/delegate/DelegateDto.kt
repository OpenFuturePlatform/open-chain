package io.openfuture.chain.domain.delegate


class DelegateDto(
    val publicKey: String,
    val info: DelegateInfo,
    val rating: Int = 0
)