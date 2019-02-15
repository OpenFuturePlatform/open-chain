package io.openfuture.chain.rpc.validation

import io.openfuture.chain.rpc.validation.annotation.Bytecode
import io.openfuture.chain.smartcontract.component.validation.SmartContractValidator
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.fromHexString
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class BytecodeValidator : ConstraintValidator<Bytecode, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (null == value) {
            return true
        }

        return SmartContractValidator.validate(fromHexString(value))
    }

}