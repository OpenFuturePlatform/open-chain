package io.openfuture.chain.smartcontract.model

class Abi {

    private val abi: MutableMap<String, List<String>> = mutableMapOf()


    fun getAbi(): String = "[${abi.map { mapMethod(it) }.joinToString(",")}]"

    fun addMethod(methodName: String, argumentTypes: List<String>) {
        abi[methodName] = argumentTypes
    }

    private fun mapMethod(mapEntry: Map.Entry<String, List<String>>): String =
        """{"name": "${mapEntry.key}","inputs": [${mapEntry.value.joinToString(",") { "\"$it\"" }}]}"""

}