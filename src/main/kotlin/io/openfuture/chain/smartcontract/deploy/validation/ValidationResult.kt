package io.test.io.openfuture.chain.smartcontract.deploy.validation

class ValidationResult(
    val errors: MutableSet<String> = mutableSetOf()
) {

    fun addError(error: String) {
        errors.add(error)
    }

    fun hasErrors(): Boolean = errors.isNotEmpty()

    override fun toString(): String = errors.joinToString(separator = "\n", prefix = "[", postfix = "]")

}