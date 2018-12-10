package io.openfuture.chain.network.component

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelId
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.AttributeKey
import io.netty.util.concurrent.GlobalEventExecutor
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.exception.NotFoundChannelException
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.serialization.Serializable
import io.openfuture.chain.network.server.ServerReadyEvent
import io.openfuture.chain.network.service.ConnectionService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

@Component
class ChannelsHolder(
    private val nodeProperties: NodeProperties,
    private val addressesHolder: AddressesHolder,
    private val nodeKeyHolder: NodeKeyHolder,
    private val connectionService: ConnectionService
) : ApplicationListener<ServerReadyEvent> {

    companion object {
        val NODE_INFO_KEY: AttributeKey<NodeInfo> = AttributeKey.valueOf<NodeInfo>("uid")
        private val log: Logger = LoggerFactory.getLogger(ChannelsHolder::class.java)
    }

    private val channelGroup = object : DefaultChannelGroup(GlobalEventExecutor.INSTANCE) {

        override fun add(element: Channel): Boolean {
            val result = super.add(element)
            findNewPeer()
            return result
        }

        override fun remove(element: Channel): Boolean {
            val result = super.remove(element)
            findNewPeer()
            return result
        }

    }

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private var connectionTask: Future<*>? = null


    override fun onApplicationEvent(event: ServerReadyEvent) {
        findNewPeer()
    }

    fun broadcast(message: Serializable) {
        channelGroup.writeAndFlush(message)
    }

    fun sendRandom(message: Serializable) {
        val channel = channelGroup.shuffled().firstOrNull()
            ?: throw NotFoundChannelException("List channels is empty")
        channel.writeAndFlush(message)
    }

    fun send(message: Serializable, nodeInfo: NodeInfo): Boolean {
        val channel = channelGroup.firstOrNull { it.attr(NODE_INFO_KEY).get() == nodeInfo } ?: return false
        channel.writeAndFlush(message)
        log.debug("Send ${message::class.java.simpleName} to ${nodeInfo.address.port}")
        return true
    }

    fun remove(channel: Channel) {
        channelGroup.remove(channel)
    }

    fun size(): Int = channelGroup.size

    fun isEmpty(): Boolean = channelGroup.isEmpty()

    fun getNodesInfo(): List<NodeInfo> = channelGroup.map { it.attr(NODE_INFO_KEY).get() }

    fun getNodeInfoByChannelId(channelId: ChannelId): NodeInfo? = channelGroup.find(channelId)?.attr(NODE_INFO_KEY)?.get()

    @Synchronized
    fun addChannel(channel: Channel, nodeInfo: NodeInfo? = null): Boolean {
        if (channelGroup.any { it.attr(NODE_INFO_KEY).get() == nodeInfo }) {
            return false
        }
        channel.attr(NODE_INFO_KEY).setIfAbsent(nodeInfo)
        log.debug("${channel.remoteAddress()} connected, operating peers count is ${channelGroup.size}")
        return channelGroup.add(channel)
    }

    @Synchronized
    fun hasChannel(channel: Channel): Boolean = channelGroup.contains(channel)

    fun findNewPeer() {
        if (nodeProperties.peersNumber!! > channelGroup.size && (null == connectionTask || connectionTask!!.isDone)) {
            connectionTask = executor.submit { findNewPeer0() }
        }
    }

    private fun findNewPeer0() {
        val addresses = addressesHolder.getNodeInfos().filter { !getNodesInfo().contains(it) }
        if (addresses.size < nodeProperties.rootNodes.size) {
            while (!findBootNode() && nodeProperties.rootNodes.size > channelGroup.size) {
                log.warn("Unable to find boot peer. Retry...")
                TimeUnit.SECONDS.sleep(3)
            }
        } else {
            while (!findRegularPeer() && nodeProperties.rootNodes.size > channelGroup.size) {
                log.warn("Unable to find regular peer. Retry...")
                TimeUnit.SECONDS.sleep(3)
            }
        }
    }

    private fun findBootNode(): Boolean {
        log.info("Searching boot peers ${channelGroup.joinToString { it.remoteAddress().toString() }} | ${channelGroup.size}")
        val connectedPeers = getNodesInfo().map { it.address }
        for (bootAddress in nodeProperties.getRootAddresses().minus(connectedPeers).shuffled()) {
            val connected = connectionService.connect(bootAddress, Consumer {
                greet(it)
            })
            if (connected) return true
        }
        return false
    }

    private fun findRegularPeer(): Boolean {
        log.info("Looking for regular peer ${channelGroup.joinToString { it.remoteAddress().toString() }} | ${channelGroup.size}")
        val knownPeers = getNodesInfo()
        for (peer in addressesHolder.getRandomList(exclude = knownPeers)) {
            val connected = connectionService.connect(peer.address, Consumer {
                greet(it)
            })
            if (connected) return true
        }
        return false
    }

    private fun greet(channel: Channel): Boolean {
        val message = GreetingMessage(nodeProperties.port!!, nodeKeyHolder.getUid())
        channel.writeAndFlush(message)
        return true
    }

}