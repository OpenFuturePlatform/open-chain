package io.openfuture.chain.service.peer

import io.openfuture.chain.domain.node.PeerDto
import io.openfuture.chain.entity.peer.Peer
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.PeerRepository
import io.openfuture.chain.service.BasePeerService
import org.springframework.transaction.annotation.Transactional


abstract class DefaultBasePeerService<Entity : Peer, Dto : PeerDto>(
    protected val repository: PeerRepository<Entity>
) : BasePeerService<Entity, Dto> {

    @Transactional(readOnly = true)
    override fun findByNetworkId(networkId: String) : Entity?{
        return repository.findOneByNetworkId(networkId)
    }

    @Transactional(readOnly = true)
    override fun getByNetworkId(networkId: String): Entity = repository.findOneByNetworkId(networkId)
        ?: throw NotFoundException("Peer with network_id: $networkId not exist!")
    
    @Transactional(readOnly = true)
    override fun findAll(): List<Entity> = repository.findAll()

    @Transactional
    override fun save(entity: Entity) {
        repository.save(entity)
    }

    @Transactional
    override fun deleteByNetworkId(networkId: String) {
        repository.deleteOneByNetworkId(networkId)
    }

    @Transactional
    override fun deleteAll() {
        repository.deleteAll()
    }

}
