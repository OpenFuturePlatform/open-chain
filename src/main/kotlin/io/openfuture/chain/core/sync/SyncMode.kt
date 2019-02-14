package io.openfuture.chain.core.sync


enum class SyncMode(private val value: Byte) {
    LIGHT(1),
    FULL(2);

    companion object {
        fun ofByte(value: Byte): SyncMode = values().find { it.value == value }
            ?: throw IllegalArgumentException("Sync mode by name: $value not found")

        fun toByte(name: SyncMode): Byte = values().find { it == name }!!.value
    }

}

