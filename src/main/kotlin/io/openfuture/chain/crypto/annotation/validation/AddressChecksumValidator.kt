package io.openfuture.chain.crypto.annotation.validation

import io.openfuture.chain.crypto.annotation.AddressChecksum
import io.openfuture.chain.crypto.util.AddressUtils
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * Mixed-case address checksum validator
 */
class AddressChecksumValidator : ConstraintValidator<AddressChecksum, String> {

    companion object {
        private const val ADDRESS_PATTERN = "^0x[0-9a-fA-F]{40}$"
    }


    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (null == value) {
            return true
        }

        if (!ADDRESS_PATTERN.toRegex().matches(value)) {
            return false
        }

        val addressWithoutPrefix = AddressUtils.removePrefix(value)
        return addressWithoutPrefix == AddressUtils.addChecksum(addressWithoutPrefix.toLowerCase())
    }

}