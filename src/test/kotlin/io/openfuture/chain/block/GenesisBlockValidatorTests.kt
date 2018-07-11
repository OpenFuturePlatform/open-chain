package io.openfuture.chain.block

import io.openfuture.chain.block.validation.GenesisBlockValidator
import io.openfuture.chain.entity.GenesisBlock
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test

class GenesisBlockValidatorTests {

    private lateinit var genesisBlockValidator: GenesisBlockValidator

    @Before
    fun setUp() {
        genesisBlockValidator = GenesisBlockValidator()
    }

    @Test
    fun isValidShouldReturnTrue() {
        val block = GenesisBlock(
            "1",
            1L,
            "1",
            "1",
            1L,
            1L,
            emptySet()
        )

        val isBlockValid = genesisBlockValidator.isValid(block)

        Assertions.assertThat(isBlockValid).isTrue()
    }

}