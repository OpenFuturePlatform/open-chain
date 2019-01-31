package io.openfuture.chain.smartcontract.component.validation

import org.objectweb.asm.ClassReader
import org.slf4j.LoggerFactory

object SmartContractValidator {

    fun validate(bytes: ByteArray): Boolean {
        val reader = ClassReader(bytes)

        val visitor = SmartContractVisitor()
        reader.accept(visitor, ClassReader.SKIP_DEBUG)

        val result = visitor.validationResult
        if (result.hasErrors()) {
            log.warn(result.getErrors().joinToString("\n\n"))
            return false
        }

        return true
    }

    private val log = LoggerFactory.getLogger(SmartContractValidator::class.java)

}