package io.openfuture.chain.core.component

import io.openfuture.chain.core.model.entity.state.State
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class StatePool {

    private val pool: MutableMap<String, State> = ConcurrentHashMap()


    fun getPool(): Map<String, State> = pool

    fun getStates(): List<State> = pool.values.toList()

    fun get(address: String): State? = pool[address]

    fun update(state: State): State? {
        state.hash = ByteUtils.toHexString(HashUtils.doubleSha256(state.getBytes()))
        return pool.put(state.address, state)
    }

    fun clear() {
        pool.clear()
    }

}