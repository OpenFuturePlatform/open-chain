package io.zensoft.netty.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.lang.IllegalArgumentException


class NettyServer(var port: Int) : Runnable {

    override fun run() {
        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup(4)
        try {
            val bootstrap = ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(NettyChannelInitializer())
                    .option(ChannelOption.SO_BACKLOG, 512)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)

            val future = bootstrap.bind(port)

            future.sync() // locking the thread until groups are going on
            future.channel().closeFuture().sync()
        } catch (e: InterruptedException) {
            throw IllegalArgumentException(e)
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }
}

fun main(args: Array<String>) {
    var server = NettyServer(8081);
    server.run();
}