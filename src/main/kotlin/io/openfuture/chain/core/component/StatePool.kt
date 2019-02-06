package io.openfuture.chain.core.component

import io.openfuture.chain.network.message.core.StateMessage
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class StatePool {

    private val pool: MutableMap<String, StateMessage> = ConcurrentHashMap()


    fun getPool(): Map<String, StateMessage> = pool

    fun getStates(): List<StateMessage> = pool.values.toList()

    fun get(address: String): StateMessage? = pool[address]

    fun update(state: StateMessage): StateMessage? = pool.put(state.address, state)

    fun clear() {
        pool.clear()
    }

}