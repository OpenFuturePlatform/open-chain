package io.openfuture.chain.domain.transaction.vote

class VoteTransactionData(
        var votes: MutableList<VoteDto>
) {

    fun getByteData(time: Long): ByteArray {
        val builder = StringBuilder()
        builder.append(time)
        builder.append(votes)
        return builder.toString().toByteArray()
    }

}