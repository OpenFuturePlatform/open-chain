package io.openfuture.chain.network.handler.network.initializer

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.handler.timeout.IdleStateHandler
import io.openfuture.chain.network.handler.consensus.BlockApprovalHandler
import io.openfuture.chain.network.handler.consensus.BlockAvailabilityRequestHandler
import io.openfuture.chain.network.handler.consensus.BlockAvailabilityResponseHandler
import io.openfuture.chain.network.handler.consensus.PendingBlockMessageHandler
import io.openfuture.chain.network.handler.core.DelegateTransactionHandler
import io.openfuture.chain.network.handler.core.TransferTransactionHandler
import io.openfuture.chain.network.handler.core.VoteTransactionHandler
import io.openfuture.chain.network.handler.network.ConnectionHandler
import io.openfuture.chain.network.handler.network.HeartBeatHandler
import io.openfuture.chain.network.handler.network.NetworkStatusHandler
import io.openfuture.chain.network.handler.network.NewClientHandler
import io.openfuture.chain.network.handler.network.client.GreetingResponseHandler
import io.openfuture.chain.network.handler.network.codec.MessageCodec
import io.openfuture.chain.network.handler.sync.*
import io.openfuture.chain.network.property.NodeProperties
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class ClientChannelInitializer(
    private val nodeProperties: NodeProperties,
    private val applicationContext: ApplicationContext,
    private val connectionHandler: ConnectionHandler,
    private val heartBeatHandler: HeartBeatHandler,
    private val greetingResponseHandler: GreetingResponseHandler,
    private val newClientHandler: NewClientHandler,
    private val networkStatusHandler: NetworkStatusHandler,
    private val epochRequestHandler: EpochRequestHandler,
    private val epochResponseHandler: EpochResponseHandler,
    private val mainBlockHandler: MainBlockHandler,
    private val genesisBlockHandler: GenesisBlockHandler,
    private val syncStatusHandler: SyncStatusHandler,
    private val transferTransactionHandler: TransferTransactionHandler,
    private val delegateTransactionHandler: DelegateTransactionHandler,
    private val voteTransactionHandler: VoteTransactionHandler,
    private val pendingBlockMessageHandler: PendingBlockMessageHandler,
    private val blockApprovalHandler: BlockApprovalHandler,
    private val blockAvailabilityRequestHandler: BlockAvailabilityRequestHandler,
    private val blockAvailabilityResponseHandler: BlockAvailabilityResponseHandler
) : ChannelInitializer<Channel>() {

    override fun initChannel(channel: Channel) {
        val readIdleTime = (nodeProperties.heartBeatInterval!! + nodeProperties.connectionTimeout!!).toLong()
        val writeIdleTime = nodeProperties.heartBeatInterval!!.toLong()

        val pipeline = channel.pipeline()

        pipeline.addLast(
            IdleStateHandler(readIdleTime, writeIdleTime, 0, TimeUnit.MILLISECONDS),
            connectionHandler,
            applicationContext.getBean(MessageCodec::class.java),
            heartBeatHandler,
            greetingResponseHandler,
            newClientHandler,

            networkStatusHandler,// blocking
            //        sync
            epochRequestHandler,
            epochResponseHandler,
            mainBlockHandler,
            genesisBlockHandler,
            blockAvailabilityResponseHandler,
            syncStatusHandler,
            blockAvailabilityRequestHandler,
            //        core
            transferTransactionHandler,
            delegateTransactionHandler,
            voteTransactionHandler,
            //        consensus
            pendingBlockMessageHandler,
            blockApprovalHandler
        )
    }

}