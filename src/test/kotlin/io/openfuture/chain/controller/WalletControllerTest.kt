package io.openfuture.chain.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.service.WalletService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean

@WebFluxTest(WalletController::class)
class WalletControllerTests : ControllerTests() {

    @MockBean
    private lateinit var walletService: WalletService


    @Test
    fun getBalanceShouldReturnWalletBalanceTest() {
        val address = "address"
        val expectedBalance = 1.0

        given(walletService.getBalance(address)).willReturn(expectedBalance)

        val actualResult = webClient.get().uri("${PathConstant.RPC}/wallets/$address/balance")
                .exchange()
                .expectStatus().isOk
                .expectBody(Double::class.java)
                .returnResult().responseBody!!

        assertThat(actualResult).isEqualTo(expectedBalance)
    }

}