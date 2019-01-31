package io.openfuture.chain.smartcontract.component.validation

import io.openfuture.chain.smartcontract.model.SmartContract
import io.openfuture.chain.smartcontract.model.ValidationResult
import io.openfuture.chain.smartcontract.util.asPackagePath
import io.openfuture.chain.smartcontract.util.asResourcePath
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.ASM6
import org.slf4j.LoggerFactory
import java.lang.reflect.Modifier

class SmartContractVisitor : ClassVisitor(ASM6) {

    val validationResult = ValidationResult()

    companion object {
        private val superContractName = SmartContract::class.java.name.asResourcePath
        private val log = LoggerFactory.getLogger(SmartContractVisitor::class.java)
    }


    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?,
                       interfaces: Array<out String>?) {
        if (null == superName || superName != superContractName) {
            validationResult.addError("Class is not a smart contract. Should inherit a $superContractName class")
        }

        interfaces?.forEach { validateType(it.asPackagePath) }

        log.debug("CLASS: name-$name, superName-$superName, signature-$signature, version-$version")
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitField(access: Int, name: String?, descriptor: String?, signature: String?, value: Any?): FieldVisitor? {
        validateType(descriptor.let { Type.getType(descriptor)?.className })

        log.debug("FIELD: name-$name, descriptor-$descriptor, signature-$signature")
        return super.visitField(access, name, descriptor, signature, value)
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        if (Modifier.isNative(access)) {
            validationResult.addError("Native methods not allowed")
        }

        if (Modifier.isSynchronized(access)) {
            validationResult.addError("Synchronized methods not allowed")
        }

        validateType(descriptor.let { Type.getReturnType(descriptor)?.className })

        log.debug("METHOD: name-$name, descriptor-$descriptor, signature-$signature, exceptions-$exceptions")
        return SourceMethodVisitor()
    }

    private fun validateType(classType: String?) {
        if (null != classType && !Whitelist.isAllowedType(classType)) {
            validationResult.addError("$classType is forbidden in the smart contract")
        }
    }


    private inner class SourceMethodVisitor : MethodVisitor(ASM6) {

        override fun visitLocalVariable(name: String?, descriptor: String?, signature: String?, start: Label?, end: Label?, index: Int) {
            if (null != name && "this" != name) {
                validateType(descriptor.let { Type.getType(descriptor)?.className })
            }

            log.debug("LOCAL_VAR: name-$name, descriptor-$descriptor")
            super.visitLocalVariable(name, descriptor, signature, start, end, index)
        }

        override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
            if (null != type && !Whitelist.isAllowedException(type.asPackagePath)) {
                validationResult.addError("Disallowed to throw ${type.asPackagePath} exception")
            }

            log.debug("TRY_CATCH: type-$type")
            super.visitTryCatchBlock(start, end, handler, type)
        }

        override fun visitTypeInsn(opcode: Int, type: String?) {
            validateType(type?.asPackagePath)

            log.debug("TYPE: type-$type")
            super.visitTypeInsn(opcode, type)
        }

    }

}
