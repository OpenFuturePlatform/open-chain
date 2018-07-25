package io.openfuture.chain.component.converter.transaction

import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.base.BaseTransaction

interface EmbeddedTransactionEntityConverter<Entity : BaseTransaction, Data : BaseTransactionData>
    : TransactionEntityConverter<Entity, Data>