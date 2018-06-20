package io.openfuture.chain.crypto

enum class Words(private val bitLength: Int) {

    TWELVE(128),
    FIFTEEN(160),
    EIGHTEEN(192),
    TWENTY_ONE(224),
    TWENTY_FOUR(256);

    fun getByteLength() = bitLength / 8

}
