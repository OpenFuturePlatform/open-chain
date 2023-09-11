package io.openfuture.chain.stress_test.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.rpc.domain.RestResponse
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.TransferTransactionResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.CountDownLatch


@RestController
@Validated
@RequestMapping("/rpc/test/transfer")
class BatchController {
   // private val concurrentRequests = 1
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    //private val latch = CountDownLatch(concurrentRequests)

    companion object {
        private const val TRANSFER_TRANSACTION_URL = "/rpc/transactions/transfer"
        private const val TRANSFER_TRANSACTION_GET_URL = "rpc/transactions/transfer/address/0x6f7c626D720044905536009AD0c637625e5F57F5?offset=0&limit=15&sortBy=timestamp&sortDirection=DESC"
    }

    @GetMapping
    fun test(@RequestParam(required = false) count: Int) : Long {
        val latch = CountDownLatch(count)
        val mapper = jacksonObjectMapper()

        /*val webClient = WebClient.builder().baseUrl("http://gunter.chain.openfuture.io:9090").build()

        val getListResponse = webClient.get()
            .uri(TRANSFER_TRANSACTION_GET_URL)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(RestResponse::class.java)
            .block()
        var initialTransactionCount = 0
        var finalTransactionCount = 0
        val javaType = mapper.typeFactory.constructParametricType(PageResponse::class.java, TransferTransactionResponse::class.java)
        if (getListResponse != null) {
            val respData: String = mapper.writeValueAsString(getListResponse.payload)
            val pageResponseList: PageResponse<TransferTransactionResponse> = mapper.readValue(respData, javaType)
            println("Total count : ${pageResponseList.totalCount}")
            initialTransactionCount = pageResponseList.totalCount.toInt()
            finalTransactionCount = initialTransactionCount
        }
        */
        val startTime = System.currentTimeMillis()
        println("STARTED POST REQUEST")

       /* Flux.range(1, count)
            .parallel()
            .runOn(Schedulers.parallel())
            .flatMap { this.sendWebRequest(webClient) }
            .doOnError{
                err -> println("WebClient err $err")
            }
            .subscribe()*/

        for (i in 1..count) {
            coroutineScope.launch {
                val request = createRandomTransferRequest()
                //println("Request $i request: $request in thread ${Thread.currentThread().name}")
                val url = urlEndpoints.random()
                println("THREAD ${Thread.currentThread().name} and url $url")
                val webClient = WebClient.builder().baseUrl(url).build()
                webClient.post()
                    .uri(TRANSFER_TRANSACTION_URL)
                    .body(Mono.just(request), TransferTransactionRequest::class.java)
                    .retrieve()
                    .bodyToMono(RestResponse::class.java)
                    .doOnError{err -> println("REQUEST ERROR ${err.localizedMessage} in $url") }
                    .subscribe()

                latch.countDown()
            }
        }
        latch.await()
        println("ENDED POST REQUEST ${(System.currentTimeMillis()-startTime)/1000}")
        //TODO - GET TIME TO UPDATE $concurrentRequests number of transactions to be updated
        println("NUMBER OF TRANSACTIONS --- $count")
        /*while (initialTransactionCount + count - 1 > finalTransactionCount){

            val getListResponseFinal = webClient.get()
                .uri(TRANSFER_TRANSACTION_GET_URL)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(RestResponse::class.java)
                .block()

            if (getListResponseFinal != null) {
                val respData: String = mapper.writeValueAsString(getListResponseFinal.payload)
                val pageResponseList: PageResponse<TransferTransactionResponse>  = mapper.readValue(respData, javaType)
                finalTransactionCount = pageResponseList.totalCount.toInt()
                println("Current Transaction count : $finalTransactionCount")
                val trxExecuted = (System.currentTimeMillis() - startTime)/1000
                val diff = finalTransactionCount - initialTransactionCount
                println("Current processed transactions : $diff in - $trxExecuted")
                if (diff > 1)
                    break
            }

            Thread.sleep(10000)
        }
        val endTime = System.currentTimeMillis()
        val executionTime = (endTime-startTime)/1000
        println("Time spent for confirmation : $executionTime seconds")
        return executionTime*/
        return count.toLong()
    }

    private fun sendWebRequest(webClient: WebClient): Mono<RestResponse<*>>{
        val request = createRandomTransferRequest()
        println("THREAD ${Thread.currentThread().name}")
        return webClient.post()
                .uri(TRANSFER_TRANSACTION_URL)
                .body(Mono.just(request), TransferTransactionRequest::class.java)
                .retrieve()
                .bodyToMono(RestResponse::class.java)
    }

    private fun createRandomTransferRequest(): TransferTransactionRequest {
        val recipientAddress = recipientAddresses.random()
        val timestamp = System.currentTimeMillis()
        val hash = Block.generateHashForTransaction(
            timestamp,
            0L,
            1L,
            "0x6f7c626D720044905536009AD0c637625e5F57F5",
            recipientAddress
        )
        val privateKey = ByteUtils.fromHexString("3fd309d4e9ae09faf06aa49405ee296c0bae79ab862f26b638e3f638aba610f3")
        val sign = SignatureUtils.sign(ByteUtils.fromHexString(hash), privateKey)

        return TransferTransactionRequest(
            timestamp = timestamp,
            fee = 0,
            hash = hash,
            senderAddress = "0x6f7c626D720044905536009AD0c637625e5F57F5",
            senderSignature = sign,
            senderPublicKey = "03609ceae592ac63503be363c99570da723bcb1e637c35c5a9044c78d826a3f79e",
            amount = 1,
            recipientAddress = recipientAddress,
            data = null
        )
    }

    private val recipientAddresses = listOf(
        "0x523a4a63d1d3Aa7CEdbb7FB9d1363DE76DdBC2FD",
        "0xE484275A491551de9944FAE8e9168bF5afA1Eb1e",
        "0x523a4a63d1d3Aa7CEdbb7FB9d1363DE76DdBC2FD",
        "0xE484275A491551de9944FAE8e9168bF5afA1Eb1e",
        "0x49661d192C5Dd3C6097BF2edec5905cc6d691649",
        "0x56e8DFcF58Af2922faf05fecd8F9c156B93C920f",
        "0x8ADD8540CB4f474E4863455f7C7D2d876Ac84475",
        "0xb1C95326174C06cafCa87140931918D522C501a7",
        "0x4e93E5D112e878672888F5aE794287B28846f252",
        "0x0A39e3C3dF795bd4Cd057A2fE381D2053eF84D7C",
        "0xacD267a983c1A415DB0B96c62D443A9ba6C75B7E",
        "0xe307b01801Ff02062aB3db937Ca0dAb836eC3EE0",
        "0xb10A654bE8bb9144481Cb072053f5C11a81bC06e",
        "0x55A763C173D3B77E2d00e8ED2Bb910E8c4a4308f",
        "0xBD23fec0B69300bb18e2a82416A05ecFc9256D74",
        "0x54466Ae803910baE12281bE814039F6457f95c62",
        "0x875CEe49e89E963003E7C02a949Ca1BA0b0dA8b5",
        "0x744e5f8Efb4dbfEC8b96F6429F9ce86904735736",
        "0x604928a2A563748A1fFd2bFA670a2CEDb3445692",
        "0xF5861731377be1b1736A29301f87C4Bf907895Db",
        "0x175D5f7aC09D6f9c4B70C11b1cd84671bc3E9F0F",
        "0xCd91F1eBb8E096dAAb1d5c2c97f5E340c6b2E155"
    )

    private val urlEndpoints = listOf(
        //"http://gunter.chain.openfuture.io:9090",
        "http://margo.chain.openfuture.io:9090",
        "http://wolf.chain.openfuture.io:9090",
        "http://charlie.chain.openfuture.io:9090",
        "http://gonzalez.chain.openfuture.io:9090",
        "http://george.chain.openfuture.io:9090",
        "http://pablo.chain.openfuture.io:9090",
        "http://anna.chain.openfuture.io:9090",
        "http://niko.chain.openfuture.io:9090",
        "http://bruce.chain.openfuture.io:9090",
        "http://chuck.chain.openfuture.io:9090")

    /*private val urlEndpoints = listOf(
        "http://gunter.chain.openfuture.io:9090")*/
}