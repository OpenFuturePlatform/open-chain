package io.openfuture.chain.service.peer

import io.openfuture.chain.domain.node.PeerDto
import io.openfuture.chain.entity.peer.Peer
import io.openfuture.chain.repository.PeerRepository
import io.openfuture.chain.service.PeerService
import org.springframework.stereotype.Service

@Service
class DefaultPeerService(
    repository: PeerRepository<Peer>
) : DefaultBasePeerService<Peer, PeerDto>(repository), PeerService {

    override fun add(dto: PeerDto): Peer = repository.save(Peer(dto))

    override fun addAll(list: List<PeerDto>) {
        val peerEntities = list.map { Peer(it) }
        repository.saveAll(peerEntities)
    }

}
