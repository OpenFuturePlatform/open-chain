package io.openfuture.chain.smartcontract.model

enum class ContractParameterType(private val value: Byte) {

    BOOLEAN(0x00),
    BYTE(0x01),
    INTEGER(0x02),
    LONG(0x03),
    CHAR(0x04),
    STRING(0x05),
    BYTE_ARRAY(0x06),
    VOID(0x07);

    fun getValue(): Byte = value

}