package io.openfuture.chain.rpc.domain

import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType.DEPLOY
import io.openfuture.chain.crypto.annotation.AddressChecksum
import io.openfuture.chain.smartcontract.component.validation.SmartContractValidator
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import javax.validation.constraints.AssertTrue

data class SmartContractEstimateRequest(
    @field:AddressChecksum var recipientAddress: String? = null,
    var data: String? = null
) {

    @AssertTrue(message = "Invalid request")
    fun isValid(): Boolean = (null != recipientAddress || null != data) &&
        when (TransferTransactionType.getType(recipientAddress, data)) {
            DEPLOY -> SmartContractValidator.validate(ByteUtils.fromHexString(data))
            else -> true
        }

}