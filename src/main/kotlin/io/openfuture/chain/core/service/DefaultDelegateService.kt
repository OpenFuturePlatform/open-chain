package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.repository.DelegateRepository
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.data.domain.Page
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateService(
    private val repository: DelegateRepository,
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val consensusProperties: ConsensusProperties
) : DelegateService {

    companion object {
        const val ID = "id"
        const val PUBLIC_KEY = "public_key"
        const val ADDRESS = "address"
    }


    @Transactional(readOnly = true)
    override fun getAll(request: PageRequest): Page<Delegate> = repository.findAll(request)

    @Transactional(readOnly = true)
    override fun getByPublicKey(key: String): Delegate = repository.findOneByPublicKey(key)
        ?: throw NotFoundException("Delegate with key: $key not exist!")

    @Transactional(readOnly = true)
    override fun getActiveDelegates(): Set<Delegate> {
        val sql = "select sum(wll.balance) rating, dg.public_key $PUBLIC_KEY, dg.address $ADDRESS, dg.id $ID " +
            "from wallets2delegates as s2d\n" +
            "  join wallets as wll on wll.id = s2d.wallet_id\n" +
            "  join delegates as dg on dg.id = s2d.delegate_id\n" +
            "group by dg.public_key\n" +
            "order by rating desc\n" +
            "limit ${consensusProperties.delegatesCount!!}"

        return jdbcTemplate.query(sql) { rs, rowNum ->
            Delegate(rs.getString(PUBLIC_KEY), rs.getString(ADDRESS), rs.getInt(ID))
        }.toSet()
    }

    @Transactional
    override fun save(delegate: Delegate): Delegate = repository.save(delegate)

}