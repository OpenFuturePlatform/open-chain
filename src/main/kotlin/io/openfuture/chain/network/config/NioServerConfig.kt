package io.openfuture.chain.network.config

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.openfuture.chain.property.NodeProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NioServerConfig(
    private val property: NodeProperty,
    private val serverChannelInitializer: ChannelInitializer<SocketChannel>
) {

    @Bean
    fun serverBootstrap(): ServerBootstrap = ServerBootstrap()
        .group(bossGroup(), workerGroup())
        .channel(NioServerSocketChannel::class.java)
        .childHandler(serverChannelInitializer)
        .option(SO_BACKLOG, property.backlog)
        .childOption(SO_KEEPALIVE, property.keepAlive)
        .childOption(CONNECT_TIMEOUT_MILLIS, property.connectionTimeout)

    @Bean(destroyMethod = "shutdownGracefully")
    fun bossGroup(): NioEventLoopGroup = NioEventLoopGroup(property.bossCount!!)

    @Bean(destroyMethod = "shutdownGracefully")
    fun workerGroup(): NioEventLoopGroup = NioEventLoopGroup()

}