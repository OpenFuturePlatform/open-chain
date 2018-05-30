package io.openfuture.chain.node.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.openfuture.chain.property.NodeProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class NodeServer(
        val nodeServerChannelInitializer: NodeServerChannelInitializer,
        val properties: NodeProperties
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent?) {
        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup(4)
        try {
            val bootstrap = ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(nodeServerChannelInitializer)

            val future = bootstrap.bind(properties.port!!).sync() // locking the thread until groups are going on
            future.channel().closeFuture().sync()
        } catch (e: InterruptedException) {
            throw IllegalArgumentException(e)
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }
}