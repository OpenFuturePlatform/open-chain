package io.openfuture.chain.validation

import io.openfuture.chain.annotation.Address
import io.openfuture.chain.crypto.util.AddressUtils
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * Mixed-case address checksum validator
 */
class AddressValidator : ConstraintValidator<Address, String> {

    companion object {
        private const val ADDRESS_PATTERN = "^0x[0-9a-fA-F]{40}$"
    }

    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
        return if (ADDRESS_PATTERN.toRegex().matches(value)) {
            val noPrefixAddress = AddressUtils.removePrefix(value)
            noPrefixAddress == AddressUtils.addChecksum(noPrefixAddress.toLowerCase())
        } else false
    }

}