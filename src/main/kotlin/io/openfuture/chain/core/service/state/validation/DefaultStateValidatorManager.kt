package io.openfuture.chain.core.service.state.validation

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.state.State
import io.openfuture.chain.core.service.StateValidatorManager
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DefaultStateValidatorManager : StateValidatorManager {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultStateValidatorManager::class.java)
    }


    override fun verify(state: State): Boolean {
        return try {
            validate(state)
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    private fun validate(state: State) {
        checkHash(state)
    }

    private fun checkHash(state: State) {
        if (state.hash != ByteUtils.toHexString(HashUtils.doubleSha256(state.getBytes()))) {
            throw ValidationException("Incorrect hash in state")
        }
    }

}