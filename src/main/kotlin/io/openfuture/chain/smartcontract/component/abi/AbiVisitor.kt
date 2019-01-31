package io.openfuture.chain.smartcontract.component.abi

import io.openfuture.chain.smartcontract.model.Abi
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM6
import org.objectweb.asm.Type
import java.lang.reflect.Modifier

class AbiVisitor : ClassVisitor(ASM6) {

    val abi = Abi()

    companion object {
        private const val INIT_METHOD = "<init>"
        private const val STATIC_INITIALIZER = "<clinit>"
    }


    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?,
                             exceptions: Array<out String>?): MethodVisitor {
        if (Modifier.isPublic(access) && name != INIT_METHOD && name != STATIC_INITIALIZER) {
            val argumentTypes = Type.getArgumentTypes(descriptor).map { it.className }
            abi.addMethod(name, argumentTypes)
        }

        return object : MethodVisitor(ASM6) {}
    }

}