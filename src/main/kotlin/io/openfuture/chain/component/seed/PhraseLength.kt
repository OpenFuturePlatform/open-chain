package io.openfuture.chain.component.seed

enum class PhraseLength(private val byteLength: Int) {

    TWELVE(16);

    fun getByteLength() = byteLength

}
