package io.openfuture.chain

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.entity.Node
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.repository.NodeRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class NodeStarter(
    private val nodeProperties: NodeProperties,
    private val nodeRepository: NodeRepository
) {

    @EventListener
    fun onApplicationReady(event: ApplicationReadyEvent) {
        val key = generateEcKey()
        populateKeyInProperties(key)
        addOwnNodeInNodeList()
    }

    fun generateEcKey() : ECKey {
        val entropy = ByteArray(64)
        SecureRandom().nextBytes(entropy)
        return ECKey(entropy)
    }

    fun populateKeyInProperties(key: ECKey) {
        nodeProperties.privateKey = key.getPrivate()
        nodeProperties.publicKey = key.public
    }

    fun addOwnNodeInNodeList() {
        val node = Node(nodeProperties.publicKey!!, nodeProperties.host!!, nodeProperties.port!!)
        nodeRepository.save(node)
    }

}