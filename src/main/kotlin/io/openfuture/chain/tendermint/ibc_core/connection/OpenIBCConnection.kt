package io.openfuture.chain.tendermint.ibc_core.connection

import ibc.core.connection.v1.MsgGrpc
import ibc.core.connection.v1.Tx
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class OpenIBCConnection : MsgGrpc.MsgImplBase() {
    override fun connectionOpenInit(
        request: Tx.MsgConnectionOpenInit?,
        responseObserver: StreamObserver<Tx.MsgConnectionOpenInitResponse>?
    ) {
        super.connectionOpenInit(request, responseObserver)
        println("OpenIBCConnection connectionOpenInit")
    }

    override fun connectionOpenTry(
        request: Tx.MsgConnectionOpenTry?,
        responseObserver: StreamObserver<Tx.MsgConnectionOpenTryResponse>?
    ) {
        super.connectionOpenTry(request, responseObserver)
        println("$request?.counterparty?.connectionId")
        println("OpenIBCConnection connectionOpenTry")
    }

    override fun connectionOpenAck(
        request: Tx.MsgConnectionOpenAck?,
        responseObserver: StreamObserver<Tx.MsgConnectionOpenAckResponse>?
    ) {
        super.connectionOpenAck(request, responseObserver)
        println("OpenIBCConnection connectionOpenAck")
    }

    override fun connectionOpenConfirm(
        request: Tx.MsgConnectionOpenConfirm?,
        responseObserver: StreamObserver<Tx.MsgConnectionOpenConfirmResponse>?
    ) {
        super.connectionOpenConfirm(request, responseObserver)
    }

    override fun updateConnectionParams(
        request: Tx.MsgUpdateParams?,
        responseObserver: StreamObserver<Tx.MsgUpdateParamsResponse>?
    ) {
        super.updateConnectionParams(request, responseObserver)
    }
}