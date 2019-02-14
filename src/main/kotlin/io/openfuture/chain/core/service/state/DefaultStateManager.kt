package io.openfuture.chain.core.service.state

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.dictionary.VoteType.AGAINST
import io.openfuture.chain.core.model.entity.dictionary.VoteType.FOR
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.state.State
import io.openfuture.chain.core.repository.StateRepository
import io.openfuture.chain.core.service.AccountStateService
import io.openfuture.chain.core.service.DelegateStateService
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultStateManager(
    private val repository: StateRepository<State>,
    private val accountStateService: AccountStateService,
    private val delegateStateService: DelegateStateService
) : StateManager {

    @Suppress("UNCHECKED_CAST")
    override fun <T : State> getLastByAddress(address: String): T {
        BlockchainLock.readLock.lock()
        try {
            return repository.findFirstByAddressOrderByBlockIdDesc(address) as? T
                ?: throw NotFoundException("State with address $address not found")
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getAllDelegateStatesByBlock(block: Block): List<DelegateState> =
        delegateStateService.getAllByBlock(block)

    override fun getAllAccountStatesByBlock(block: Block): List<AccountState> =
        accountStateService.getAllByBlock(block)

    override fun getWalletBalanceByAddress(address: String): Long = accountStateService.getBalanceByAddress(address)

    override fun getVotesForDelegate(delegateKey: String): List<AccountState> =
        accountStateService.getVotesForDelegate(delegateKey)

    override fun updateWalletBalanceByAddress(address: String, amount: Long) {
        val state = accountStateService.updateBalanceByAddress(address, amount)
        if (null != state.voteFor) {
            delegateStateService.updateRating(state.voteFor!!, amount)
        }
    }

    override fun updateVoteByAddress(address: String, delegateKey: String, voteType: VoteType) {
        when (voteType) {
            FOR -> {
                val accountState = accountStateService.updateVoteByAddress(address, delegateKey)
                delegateStateService.updateRating(delegateKey, accountState.balance)
            }
            AGAINST -> {
                val accountState = accountStateService.updateVoteByAddress(address, null)
                delegateStateService.updateRating(delegateKey, -accountState.balance)
            }
        }
    }

    override fun updateSmartContractStorage(address: String, storage: String) {
        accountStateService.updateStorage(address, storage)
    }

    override fun getAllDelegates(request: PageRequest): List<DelegateState> =
        delegateStateService.getAllDelegates(request)

    override fun getActiveDelegates(): List<DelegateState> =
        delegateStateService.getActiveDelegates()

    override fun isExistsDelegateByPublicKey(key: String): Boolean = delegateStateService.isExistsByPublicKey(key)

    override fun addDelegate(delegateKey: String, walletAddress: String, createDate: Long) {
        delegateStateService.addDelegate(delegateKey, walletAddress, createDate)
    }

    override fun updateDelegateRating(delegateKey: String, amount: Long) {
        delegateStateService.updateRating(delegateKey, amount)
    }

    @Transactional
    override fun commit(state: State) {
        BlockchainLock.writeLock.lock()
        try {
            repository.save(state)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun verify(state: State): Boolean =
        state.hash != ByteUtils.toHexString(HashUtils.doubleSha256(state.getBytes()))

    @Transactional
    override fun deleteBlockStates(blockHeights: List<Long>) {
        BlockchainLock.writeLock.lock()
        try {
            repository.deleteAllByBlockHeightIn(blockHeights)
            repository.flush()
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

}