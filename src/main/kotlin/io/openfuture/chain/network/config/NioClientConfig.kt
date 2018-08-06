package io.openfuture.chain.network.config

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelOption.SO_KEEPALIVE
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.openfuture.chain.network.handler.ChannelInitializer
import io.openfuture.chain.network.handler.ConnectionClientHandler
import io.openfuture.chain.network.property.NodeProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NioClientConfig(
    private val properties: NodeProperties,
    private val applicationContext: ApplicationContext
) {

    @Bean
    fun clientBootstrap(): Bootstrap = Bootstrap()
        .group(clientGroup())
        .channel(NioSocketChannel::class.java)
        .handler(ChannelInitializer(ConnectionClientHandler::class, applicationContext))
        .option(SO_KEEPALIVE, properties.keepAlive)

    @Bean(destroyMethod = "shutdownGracefully")
    fun clientGroup(): NioEventLoopGroup = NioEventLoopGroup()

}