package io.openfuture.chain.smartcontract.model

class ValidationResult {

    private val errors: MutableSet<String> = mutableSetOf()


    fun getErrors(): Set<String> = errors

    fun addError(error: String) {
        errors.add(error)
    }

    fun hasErrors(): Boolean = errors.isNotEmpty()

}