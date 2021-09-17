package br.com.zup.academy.erombi.model.annotation

import br.com.zup.academy.erombi.repository.KeyRepository
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import jakarta.inject.Singleton
import javax.validation.Constraint
import kotlin.reflect.KClass


@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UniqueKeyValidator::class])
annotation class UniqueKey (
    val message: String = "Key já existente !",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Any>> = [],
)

@Singleton
class UniqueKeyValidator(
    val repository: KeyRepository
) : ConstraintValidator<UniqueKey, String> {

    override fun isValid(value: String?,
                         annotationMetadata: AnnotationValue<UniqueKey>,
                         context: ConstraintValidatorContext): Boolean {

        return value?.let { key ->
            return !repository.existsByKey(key)
        } ?: true

    }

}