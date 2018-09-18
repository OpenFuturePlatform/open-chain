package io.openfuture.chain.crypto.annotation

import io.openfuture.chain.crypto.annotation.validation.AddressChecksumValidator
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AddressChecksumValidator::class])
annotation class AddressChecksum(
    val message: String = "Address is not valid",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)