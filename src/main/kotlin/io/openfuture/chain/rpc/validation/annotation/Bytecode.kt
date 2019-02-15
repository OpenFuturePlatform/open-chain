package io.openfuture.chain.rpc.validation.annotation

import io.openfuture.chain.rpc.validation.BytecodeValidator
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [BytecodeValidator::class])
annotation class Bytecode(
    val message: String = "Invalid bytecode",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)