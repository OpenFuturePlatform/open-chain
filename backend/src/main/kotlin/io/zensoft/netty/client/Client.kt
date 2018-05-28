package io.zensoft.netty.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class Client {

    fun runClient(port: Int) {
        val host = "localhost"
        val workerGroup = NioEventLoopGroup()

        try {
            val bootstrap = Bootstrap()
            bootstrap.group(workerGroup)
            bootstrap.channel(NioSocketChannel::class.java)
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
            bootstrap.handler(NettyChannelInitializer())

            val future = bootstrap.connect(host, port).sync()
            val channel = future.channel()

            while (true) {
                val message = readConsole()

                if ("exit" == message) {
                    break
                }

                channel.write(message)
                channel.flush();
            }

            channel.closeFuture().sync()
        } catch (e: InterruptedException) {
        } finally {
            workerGroup.shutdownGracefully()
        }
    }
}

fun readConsole(): String {
    val input = BufferedReader(InputStreamReader(System.`in`))
    try {
        return input.readLine()
    } catch (e: IOException) {
        throw IllegalArgumentException(e)
    }

}

fun main(args: Array<String>) {
    var client = Client();
    client.runClient(8081);
}