package br.com.zup.academy.erombi.model.annotation

import br.com.zup.academy.erombi.repository.KeyRepository
import br.com.zup.academy.erombi.service.form.RemoveKeyForm
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import jakarta.inject.Singleton
import java.util.*
import javax.validation.Constraint
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidaClienteKeyValidator::class])
annotation class ValidaClienteKey(
    val message: String = "Key não encontrada ou em formato inválido !",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Any>> = [],
)

@Singleton
class ValidaClienteKeyValidator(
    val repository: KeyRepository
) : ConstraintValidator<ValidaClienteKey, RemoveKeyForm> {


    override fun isValid(
        value: RemoveKeyForm?,
        annotationMetadata: AnnotationValue<ValidaClienteKey>,
        context: ConstraintValidatorContext
    ): Boolean {

        return value?.let { form ->
            repository.existsByIdAndTitularUuidCliente(UUID.fromString(form.idKey), UUID.fromString(form.idCliente))
        } ?: false

    }

}