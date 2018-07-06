package io.openfuture.chain.domain.transaction.payload

import io.openfuture.chain.domain.transaction.vote.VoteDto
import io.openfuture.chain.entity.dictionary.TransactionPayloadType

class VotePayload(
        var votes: MutableList<VoteDto>
) : TransactionPayload(TransactionPayloadType.VOTE)