package io.openfuture.chain.network.config

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.openfuture.chain.network.handler.network.initializer.ClientChannelInitializer
import io.openfuture.chain.network.handler.network.initializer.ServerChannelInitializer
import io.openfuture.chain.network.property.NodeProperties
import org.apache.commons.net.ntp.NTPUDPClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@Configuration
class NettyConfig(
    private val serverChannelInitializer: ServerChannelInitializer,
    private val clientChannelInitializer: ClientChannelInitializer,
    private val properties: NodeProperties
) {

    @Bean
    fun taskScheduler(): ScheduledExecutorService = Executors.newScheduledThreadPool(5)

    @Bean(destroyMethod = "shutdownGracefully")
    fun bossGroup(): NioEventLoopGroup = NioEventLoopGroup()

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
    fun clientBootstrap(): Bootstrap = Bootstrap()
        .group(workerGroup())
        .channel(NioSocketChannel::class.java)
        .handler(clientChannelInitializer)
        .option(SO_KEEPALIVE, properties.keepAlive!!)

    @Bean
    fun ntpClient(): NTPUDPClient = NTPUDPClient().apply { defaultTimeout = 3000 }

}