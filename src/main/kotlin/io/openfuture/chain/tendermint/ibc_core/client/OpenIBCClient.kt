package io.openfuture.chain.tendermint.ibc_core.client

import ibc.core.client.v1.MsgGrpc
import ibc.core.client.v1.Tx
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class OpenIBCClient : MsgGrpc.MsgImplBase() {

    override fun createClient(
        request: Tx.MsgCreateClient?,
        responseObserver: StreamObserver<Tx.MsgCreateClientResponse>?
    ) {
        super.createClient(request, responseObserver)
        println("OpenIBCClient createClient")
    }

    override fun updateClient(
        request: Tx.MsgUpdateClient?,
        responseObserver: StreamObserver<Tx.MsgUpdateClientResponse>?
    ) {
        super.updateClient(request, responseObserver)
    }

    override fun upgradeClient(
        request: Tx.MsgUpgradeClient?,
        responseObserver: StreamObserver<Tx.MsgUpgradeClientResponse>?
    ) {
        super.upgradeClient(request, responseObserver)
    }

    override fun submitMisbehaviour(
        request: Tx.MsgSubmitMisbehaviour?,
        responseObserver: StreamObserver<Tx.MsgSubmitMisbehaviourResponse>?
    ) {
        super.submitMisbehaviour(request, responseObserver)
    }

    override fun recoverClient(
        request: Tx.MsgRecoverClient?,
        responseObserver: StreamObserver<Tx.MsgRecoverClientResponse>?
    ) {
        super.recoverClient(request, responseObserver)
    }

    override fun iBCSoftwareUpgrade(
        request: Tx.MsgIBCSoftwareUpgrade?,
        responseObserver: StreamObserver<Tx.MsgIBCSoftwareUpgradeResponse>?
    ) {
        super.iBCSoftwareUpgrade(request, responseObserver)
    }

    override fun updateClientParams(
        request: Tx.MsgUpdateParams?,
        responseObserver: StreamObserver<Tx.MsgUpdateParamsResponse>?
    ) {
        super.updateClientParams(request, responseObserver)
    }
}