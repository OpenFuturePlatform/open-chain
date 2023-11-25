package io.openfuture.chain.tendermint.ibc_core

import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import planet.blog.MsgGrpc
import planet.blog.Tx

@GrpcService
class OpenIBCBlogMessage : MsgGrpc.MsgImplBase() {
    override fun sendIbcPost(
        request: Tx.MsgSendIbcPost?,
        responseObserver: StreamObserver<Tx.MsgSendIbcPostResponse>?
    ) {
        super.sendIbcPost(request, responseObserver)
        println("$request?.channelID")
        println("SEND IBC PACKAGE")
    }
}