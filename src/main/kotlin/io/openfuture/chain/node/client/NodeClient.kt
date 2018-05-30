package io.openfuture.chain.node.client

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.util.CharsetUtil
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*


@Component
@Scope("prototype")
class NodeClient(val nodeClientChannelInitializer: NodeClientChannelInitializer) : Thread() {

    val random: Random = Random()
    val minSleepTime: Long = 1

    var host: String = ""
    var port: Int = 9180

    override fun run() {
        val workerGroup = NioEventLoopGroup()
        try {
            val bootstrap = Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(nodeClientChannelInitializer)

            val future = bootstrap.connect(host, port).sync()
            val channel = future.channel()
            while (true) {
                channel.write(Unpooled.copiedBuffer(random.nextInt(20).toString(), CharsetUtil.UTF_8))
                channel.flush()

                Thread.sleep(minSleepTime)
            }
            channel.closeFuture().sync()
        } catch (e: InterruptedException) {
            throw IllegalArgumentException(e)
        } finally {
            workerGroup.shutdownGracefully()
        }
    }
}