package io.openfuture.chain.smartcontract.deploy.calculation

class OpcodeDictionary {

    private val opcodeDictionary: MutableMap<Int, Long> = hashMapOf()

    private val methodDictionary: MutableMap<String, Long> = hashMapOf()

    init {
        val opcodes = this::class.java.classLoader.getResourceAsStream("pricing/opcodes.csv")
        opcodes.bufferedReader().lines().skip(1).forEach {
            val values = it.split(',')
            val opcode = values[0].toInt()
            val cost = values[1].toLong()
            opcodeDictionary[opcode] = cost
        }

        val methods = this::class.java.classLoader.getResourceAsStream("pricing/methods.csv")
        methods.bufferedReader().lines().skip(1).forEach {
            val values = it.split(',')
            val opcode = values[0]
            val cost = values[1].toLong()
            methodDictionary[opcode] = cost
        }
    }


    fun get(opcode: Int) = opcodeDictionary.getOrDefault(opcode, 0L)

    fun get(owner: String, name: String = "") = methodDictionary["$owner/$name"] ?: methodDictionary.getOrDefault(owner, 0L)

}