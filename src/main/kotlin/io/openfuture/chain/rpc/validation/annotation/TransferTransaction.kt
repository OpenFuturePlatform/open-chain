package io.openfuture.chain.rpc.validation.annotation

import io.openfuture.chain.rpc.validation.TransferTransactionValidator
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [TransferTransactionValidator::class])
annotation class TransferTransaction(
    val message: String = "Invalid transfer transaction",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)