package io.openfuture.chain.rpc.validation

import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.validation.annotation.SortConstraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class SortValidator : ConstraintValidator<SortConstraint, PageRequest> {

    override fun isValid(pageRequest: PageRequest, context: ConstraintValidatorContext): Boolean =
        pageRequest.maySortBy.keys.containsAll(pageRequest.sortBy)

}