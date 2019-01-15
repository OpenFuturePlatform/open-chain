package io.openfuture.chain.core.sync


enum class SyncMode {
    LIGHT,
    FULL;

    companion object {

        fun ofByte(id: Byte): SyncMode = values()[id.toInt()]

        fun toByte(name: SyncMode): Int {
            for (i in 0..values().size) {
                if (values()[i] == name) {
                    return i
                }
            }
            throw IllegalArgumentException("Sync mode by name: $name not found")
        }
    }
}

