package io.openfuture.chain.network.config

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption.*
import io.netty.channel.group.ChannelGroup
import io.netty.channel.group.DefaultChannelGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.util.AttributeKey
import io.netty.util.concurrent.GlobalEventExecutor
import io.openfuture.chain.network.handler.network.initializer.ClientChannelInitializer
import io.openfuture.chain.network.handler.network.initializer.ServerChannelInitializer
import io.openfuture.chain.network.property.NodeProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@Configuration
class NettyConfig(
    private val serverChannelInitializer: ServerChannelInitializer,
    private val clientChannelInitializer: ClientChannelInitializer,
    private val properties: NodeProperties
) {

    @Bean(destroyMethod = "shutdownGracefully")
    fun bossGroup(): NioEventLoopGroup = NioEventLoopGroup(properties.bossCount!!)

    @Bean(destroyMethod = "shutdownGracefully")
    fun workerGroup(): NioEventLoopGroup = NioEventLoopGroup()

    @Bean
    fun serverBootstrap(): ServerBootstrap = ServerBootstrap()
        .group(bossGroup(), workerGroup())
        .channel(NioServerSocketChannel::class.java)
        .childHandler(serverChannelInitializer)
        .option(SO_BACKLOG, properties.backlog!!)
        .childOption(SO_KEEPALIVE, properties.keepAlive!!)
        .childOption(CONNECT_TIMEOUT_MILLIS, properties.connectionTimeout!!)

    @Bean
    fun clientBootstrap(): Bootstrap {
        val heartBeatIntervalKey = AttributeKey.valueOf<Long>("heart-beat-interval")
        val connectionTimeoutKey = AttributeKey.valueOf<Long>("connection-timeout")

        return Bootstrap()
            .group(workerGroup())
            .channel(NioSocketChannel::class.java)
            .handler(clientChannelInitializer)
            .attr(heartBeatIntervalKey, properties.heartBeatInterval!!.toLong())
            .attr(connectionTimeoutKey, properties.connectionTimeout!!.toLong())
            .option(SO_KEEPALIVE, properties.keepAlive!!)
    }

    @Bean(name = ["peerChannelGroup"], destroyMethod = "close")
    fun peerChannelGroup(): ChannelGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)

    @Bean(name = ["serverChannelGroup"], destroyMethod = "close")
    fun serverChannelGroup(): ChannelGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)

}