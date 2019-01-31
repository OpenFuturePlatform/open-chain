package io.openfuture.chain.smartcontract.component

import io.openfuture.chain.smartcontract.component.validation.SmartContractValidator
import io.openfuture.chain.smartcontract.exception.SmartContractValidationException
import io.openfuture.chain.smartcontract.util.asResourcePath
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.SimpleRemapper
import org.slf4j.LoggerFactory

object ByteCodeProcessor {

    fun getClassName(bytes: ByteArray): String = ClassReader(bytes).className

    fun processByteArray(bytes: ByteArray, newName: String): ByteArray {
        val reader = ClassReader(bytes)

        validate(reader)

        val writer = ClassWriter(0)

        renameClass(reader, writer, newName)

        return writer.toByteArray()
    }

    private fun validate(reader: ClassReader) {
        val validator = SmartContractValidator()
        reader.accept(validator, ClassReader.SKIP_DEBUG)

        val result = validator.validationResult
        if (result.hasErrors()) {
            log.warn(result.getErrors().joinToString("\n\n"))
            throw SmartContractValidationException("Contract class is invalid")
        }
    }

    private fun renameClass(reader: ClassReader, writer: ClassWriter, newName: String) {
        val oldName = reader.className.asResourcePath

        val adapter = ClassRemapper(writer, SimpleRemapper(oldName, newName))
        reader.accept(adapter, ClassReader.EXPAND_FRAMES)
    }

    private val log = LoggerFactory.getLogger(ByteCodeProcessor::class.java)

}