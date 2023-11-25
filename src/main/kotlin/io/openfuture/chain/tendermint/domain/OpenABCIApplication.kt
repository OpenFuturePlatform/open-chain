package io.openfuture.chain.tendermint.domain

import com.google.protobuf.ByteString
import io.grpc.stub.StreamObserver
import io.openfuture.chain.core.model.entity.tendermint.TendermintTransferTransaction
import io.openfuture.chain.core.service.TendermintTransactionManager
import net.devh.boot.grpc.server.service.GrpcService
import tendermint.abci.ABCIApplicationGrpc
import tendermint.abci.Types
import java.nio.charset.Charset

@GrpcService
class OpenABCIApplication(
    private val transactionManager: TendermintTransactionManager
) : ABCIApplicationGrpc.ABCIApplicationImplBase() {

    override fun echo(req: Types.RequestEcho, responseObserver: StreamObserver<Types.ResponseEcho>) {
        val resp = Types.ResponseEcho.newBuilder().build()
        println("ECHO")
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun info(req: Types.RequestInfo, responseObserver: StreamObserver<Types.ResponseInfo>) {
        val resp = Types.ResponseInfo.newBuilder().build()
        println("INFO")
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun flush(request: Types.RequestFlush?, responseObserver: StreamObserver<Types.ResponseFlush>) {
        val resp = Types.ResponseFlush.newBuilder().build()
        println("FLUSH")
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun setOption(req: Types.RequestSetOption, responseObserver: StreamObserver<Types.ResponseSetOption>) {
        val resp = Types.ResponseSetOption.newBuilder().build()
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun checkTx(req: Types.RequestCheckTx, responseObserver: StreamObserver<Types.ResponseCheckTx>) {
        println("CHECK TX ${req.tx.toStringUtf8()}")
        val transfer = req.tx.parse()
        //println(transfer)
        val code = if (transactionManager.check(TendermintTransferTransaction.of(transfer))) 0 else 1

        val resp = Types.ResponseCheckTx.newBuilder()
            .setCode(code)
            .setData(req.tx)
            .setGasWanted(1)
            .build()

        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun initChain(req: Types.RequestInitChain, responseObserver: StreamObserver<Types.ResponseInitChain>) {
        val resp = Types.ResponseInitChain.newBuilder().build()
        println("INIT CHAIN")
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun beginBlock(req: Types.RequestBeginBlock, responseObserver: StreamObserver<Types.ResponseBeginBlock>) {
        println("BEGIN BLOCK")
        val resp = Types.ResponseBeginBlock.newBuilder().build()
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun deliverTx(req: Types.RequestDeliverTx, responseObserver: StreamObserver<Types.ResponseDeliverTx>) {
        println("DELIVER ${req.tx.toStringUtf8()}")

        val transfer = req.tx.parse()
        val code = if (transactionManager.add(TendermintTransferTransaction.of(transfer))) 0 else 1
        println(code)
        val resp = Types.ResponseDeliverTx.newBuilder()
            .setCode(code)
            .build()
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun endBlock(req: Types.RequestEndBlock, responseObserver: StreamObserver<Types.ResponseEndBlock>) {
        val resp = Types.ResponseEndBlock.newBuilder().build()
        println("END BLOCK")
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun commit(req: Types.RequestCommit, responseObserver: StreamObserver<Types.ResponseCommit>) {
        println("COMMIT")
        val resp = Types.ResponseCommit.newBuilder()
            .setData(ByteString.copyFrom(ByteArray(8)))
            .build()
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun query(req: Types.RequestQuery, responseObserver: StreamObserver<Types.ResponseQuery>) {
        println("QUERY")
        val builder = Types.ResponseQuery.newBuilder()
        println(req.data.toString(Charset.defaultCharset()))
        /*val parts = req.data.split('=')
        val key = parts[0]
        val value = parts[1]

        println(value.toString(Charset.defaultCharset()))
        val builder = Types.ResponseQuery.newBuilder()
        if (key.toString(Charset.defaultCharset()) == "address") {
            val transfers = transactionManager.getAllTransferTransactions(value.toString(Charset.defaultCharset()))
            println(transfers.size)
            builder.setCode(0)
            builder.log = transfers.size.toString()
        } else {
            builder.log = "does not exist"
        }*/
        builder.setCode(0)
        responseObserver.onNext(builder.build())
        responseObserver.onCompleted()
    }

    private fun ByteString.parse(): TendermintTransactionRequest {
        val parts = this.toString(Charset.defaultCharset()).split(",").toTypedArray()

        val transfer = TendermintTransactionRequest()

        for (part in parts) {
            val params = part.split('=')
            val key = params[0]
            val value = params[1]

            when (key) {
                "timestamp" -> transfer.timestamp = value.toLong()
                "fee" -> transfer.fee = value.toLong()
                "hash" -> transfer.hash = value
                "senderAddress" -> transfer.senderAddress = value
                "senderPublicKey" -> transfer.senderPublicKey = value
                "senderSignature" -> transfer.senderSignature = value.replace(' ', '+')
                "amount" -> transfer.amount = value.toLong()
                "recipientAddress" -> transfer.recipientAddress = value
            }
        }

        return transfer
    }

    private fun ByteString.split(separator: Char): List<ByteArray> {
        val arr = this.toByteArray()
        val i = (0 until this.size()).firstOrNull { arr[it] == separator.toByte() }
            ?: return emptyList()
        return listOf(
            this.substring(0, i).toByteArray(),
            this.substring(i + 1).toByteArray()
        )
    }

}