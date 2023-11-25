package io.openfuture.chain.tendermint.ibc_core.channel

import ibc.core.channel.v1.MsgGrpc
import ibc.core.channel.v1.Tx
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService


@GrpcService
class OpenIBCChannel : MsgGrpc.MsgImplBase() {

    override fun channelOpenInit(
        request: Tx.MsgChannelOpenInit?,
        responseObserver: StreamObserver<Tx.MsgChannelOpenInitResponse>?
    ) {
        super.channelOpenInit(request, responseObserver)

        println("OpenIBCChannel channelOpenInit")
    }

    override fun channelOpenTry(
        request: Tx.MsgChannelOpenTry?,
        responseObserver: StreamObserver<Tx.MsgChannelOpenTryResponse>?
    ) {
        super.channelOpenTry(request, responseObserver)
        println("OpenIBCChannel channelOpenTry")
    }

    override fun channelOpenAck(
        request: Tx.MsgChannelOpenAck?,
        responseObserver: StreamObserver<Tx.MsgChannelOpenAckResponse>?
    ) {
        super.channelOpenAck(request, responseObserver)
        println("OpenIBCChannel channelOpenAck")
    }

    override fun channelOpenConfirm(
        request: Tx.MsgChannelOpenConfirm?,
        responseObserver: StreamObserver<Tx.MsgChannelOpenConfirmResponse>?
    ) {
        super.channelOpenConfirm(request, responseObserver)
    }

    override fun channelCloseInit(
        request: Tx.MsgChannelCloseInit?,
        responseObserver: StreamObserver<Tx.MsgChannelCloseInitResponse>?
    ) {
        super.channelCloseInit(request, responseObserver)
    }

    override fun channelCloseConfirm(
        request: Tx.MsgChannelCloseConfirm?,
        responseObserver: StreamObserver<Tx.MsgChannelCloseConfirmResponse>?
    ) {
        super.channelCloseConfirm(request, responseObserver)
    }

    override fun recvPacket(request: Tx.MsgRecvPacket?, responseObserver: StreamObserver<Tx.MsgRecvPacketResponse>?) {
        super.recvPacket(request, responseObserver)
    }

    override fun timeout(request: Tx.MsgTimeout?, responseObserver: StreamObserver<Tx.MsgTimeoutResponse>?) {
        super.timeout(request, responseObserver)
    }

    override fun timeoutOnClose(
        request: Tx.MsgTimeoutOnClose?,
        responseObserver: StreamObserver<Tx.MsgTimeoutOnCloseResponse>?
    ) {
        super.timeoutOnClose(request, responseObserver)
    }

    override fun acknowledgement(
        request: Tx.MsgAcknowledgement?,
        responseObserver: StreamObserver<Tx.MsgAcknowledgementResponse>?
    ) {
        super.acknowledgement(request, responseObserver)
    }
}