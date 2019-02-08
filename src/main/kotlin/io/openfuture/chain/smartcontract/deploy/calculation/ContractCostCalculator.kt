package io.openfuture.chain.smartcontract.deploy.calculation

import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.springframework.stereotype.Component

@Component
class ContractCostCalculator {

    private val dictionary = OpcodeDictionary()


    fun calculateCost(bytes: ByteArray): Long {
        var result = 0L
        val classReader = ClassReader(bytes)
        val classNode = ClassNode()
        classReader.accept(classNode, 0)
        val methods = classNode.methods
        for (method in methods) {
            result += calculateMethodCost(method)
        }
        return result
    }

    private fun calculateMethodCost(method: MethodNode): Long {
        var result = 0L
        val instructions = method.instructions
        if (instructions.size() == 0) {
            return result
        }
        var instruction = instructions.first
        while (null != instruction) {
            val opcode = instruction.opcode
            result += when (instruction) {
                is MethodInsnNode -> dictionary.get(instruction.owner, instruction.name)
                is LdcInsnNode -> {
                    val const = instruction.cst
                    val cost = dictionary.get(instruction.cst::class.java.name.replace('.', '/'))
                    cost * ((const as? String)?.length ?: 1)
                }
                else -> dictionary.get(opcode)
            }
            instruction = instruction.next
        }
        return result
    }

}