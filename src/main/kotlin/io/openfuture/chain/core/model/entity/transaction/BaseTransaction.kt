package io.openfuture.chain.core.model.entity.transaction

import io.openfuture.chain.core.model.entity.base.BaseModel
import javax.persistence.Embedded
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class BaseTransaction(

    @Embedded
    val header: TransactionHeader,

    @Embedded
    val footer: TransactionFooter

) : BaseModel()