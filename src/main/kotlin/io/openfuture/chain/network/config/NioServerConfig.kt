package io.openfuture.chain.network.config

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.openfuture.chain.property.NodeProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NioServerConfig(
        private val properties: NodeProperties,
        private val serverChannelInitializer: ChannelInitializer<SocketChannel>
) {

    @Bean
    fun serverBootstrap(): ServerBootstrap = ServerBootstrap()
        .group(bossGroup(), workerGroup())
        .channel(NioServerSocketChannel::class.java)
        .childHandler(serverChannelInitializer)
        .option(SO_BACKLOG, properties.backlog)
        .childOption(SO_KEEPALIVE, properties.keepAlive)
        .childOption(CONNECT_TIMEOUT_MILLIS, properties.connectionTimeout)

    @Bean(destroyMethod = "shutdownGracefully")
    fun bossGroup(): NioEventLoopGroup = NioEventLoopGroup(properties.bossCount!!)

    @Bean(destroyMethod = "shutdownGracefully")
    fun workerGroup(): NioEventLoopGroup = NioEventLoopGroup()

}