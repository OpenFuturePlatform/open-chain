package io.openfuture.chain.component.seed

enum class PhraseLength(private val byteLength: Int) {

    TWELVE(SeedGeneratorConstant.ENTROPY_BYTE_SIZE);

    fun getByteLength() = byteLength

}
