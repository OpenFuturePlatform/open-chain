package io.openfuture.chain.domain.transaction.payload

import io.openfuture.chain.domain.transaction.DelegateDto
import io.openfuture.chain.entity.dictionary.TransactionPayloadType

class DelegateRegistrationPayload(
        var delegate: DelegateDto
) : TransactionPayload(TransactionPayloadType.DELEGATE_REGISTRATION)