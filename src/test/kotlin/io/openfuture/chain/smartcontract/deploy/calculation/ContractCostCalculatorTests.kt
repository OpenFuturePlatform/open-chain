package io.openfuture.chain.smartcontract.deploy.calculation

import org.assertj.core.api.Assertions
import org.junit.Test

class ContractCostCalculatorTests {

    private val costCalculator: ContractCostCalculator = ContractCostCalculator()

    @Test
    fun testCalculation() {
        val bytes = this::class.java.getResourceAsStream("/classes/SampleClass.class").readBytes()

        val result = costCalculator.calculateCost(bytes)

        Assertions.assertThat(result).isEqualTo(56L)
    }

}