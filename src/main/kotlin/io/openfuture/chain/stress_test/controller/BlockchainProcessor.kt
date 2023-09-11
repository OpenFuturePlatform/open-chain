package io.openfuture.chain.stress_test.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.openfuture.chain.rpc.domain.RestResponse
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.transaction.response.TransferTransactionResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class BlockchainProcessor {

    private var initialTransactionCount = 0
    var startTime = System.currentTimeMillis()
    val webClient : WebClient = WebClient.builder().baseUrl("http://gonzalez.chain.openfuture.io:9090").build()
    private val mapper : ObjectMapper = jacksonObjectMapper()
    val javaType = mapper.typeFactory.constructParametricType(PageResponse::class.java, TransferTransactionResponse::class.java)
    private val TRANSFER_TRANSACTION_GET_URL : String = "/rpc/transactions/transfer?limit=7&sortDirection=DESC&sortBy=timestamp"

    @Scheduled(fixedDelayString = "10000", initialDelay = 1000)
    fun process() = GlobalScope.launch {

        val getListResponseFinal = webClient.get()
            .uri(TRANSFER_TRANSACTION_GET_URL)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(RestResponse::class.java)
            .block()

        if (getListResponseFinal != null) {
            val respData: String = mapper.writeValueAsString(getListResponseFinal.payload)
            val pageResponseList: PageResponse<TransferTransactionResponse> = mapper.readValue(respData, javaType)
            val finalTransactionCount = pageResponseList.totalCount.toInt()
            println("Current Transaction count : $finalTransactionCount")
            val diff = finalTransactionCount - initialTransactionCount
            println("Time passed from last update ${(System.currentTimeMillis() - startTime)/1000}'s")

            if (diff > 1) {
                //println("Current Transaction count : $finalTransactionCount")
                initialTransactionCount = finalTransactionCount
                val endTime = System.currentTimeMillis()
                val trxExecuted = (endTime - startTime)/1000
                startTime = endTime
                println("Current processed transactions : $diff in - $trxExecuted")
            }
        }

    }
}