package io.openfuture.chain.smartcontract.model

class Contract(
    private val address: String,
    private val script: Array<Byte>,
    private val scriptHash: Array<Byte>
)