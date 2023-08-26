package io.openfuture.chain

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
class Application(
    private val transactionManager: TransactionManager
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val generateHashForTransaction: String = Block.generateHashForTransaction(
            1692629194735L,
            0L,
            1L,
            "0x6f7c626D720044905536009AD0c637625e5F57F5",
            "0x523a4a63d1d3Aa7CEdbb7FB9d1363DE76DdBC2FD"
        )
        println("++++++++" + generateHashForTransaction)
        println("3fd309d4e9ae09faf06aa49405ee296c0bae79ab862f26b638e3f638aba610f3".length)
        val fromHexString = ByteUtils.fromHexString("3fd309d4e9ae09faf06aa49405ee296c0bae79ab862f26b638e3f638aba610f3")
        val sign: String = SignatureUtils.sign(generateHashForTransaction.toByteArray(), fromHexString)

        println("++++++++" + sign)
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}

//String publicKey = "03609ceae592ac63503be363c99570da723bcb1e637c35c5a9044c78d826a3f79e";
//        String prvKey = "3fd309d4e9ae09faf06aa49405ee296c0bae79ab862f26b638e3f638aba610f3";
//
//        Transfer transfer = new Transfer("03609ceae592ac63503be363c99570da723bcb1e637c35c5a9044c78d826a3f79e", 1692629194735L,
//        "0", "0x6f7c626D720044905536009AD0c637625e5F57F5","1", "0x523a4a63d1d3Aa7CEdbb7FB9d1363DE76DdBC2FD");
//
//        URL url = new URL ("http://gunter.chain.openfuture.io:9090/rpc/transactions/tran


//MEQCIBoY+i+R7y3waxP2vyRFOKXZLAdKtaDrFTzEyyEP1w11AiBXXrNMs2nHwvn+bMlSwBnpVsqJQ3jKvi/NlZNsgxjAqA==
//MEQCIBoY+i+R7y3waxP2vyRFOKXZLAdKtaDrFTzEyyEP1w11AiBXXrNMs2nHwvn+bMlSwBnpVsqJQ3jKvi/NlZNsgxjAqA==