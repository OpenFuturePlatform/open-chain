package io.openfuture.chain.consensus.service

interface ConsensusService {

    fun getCurrentEpochHeight(): Long

    fun isGenesisBlockNeeded(): Boolean

}