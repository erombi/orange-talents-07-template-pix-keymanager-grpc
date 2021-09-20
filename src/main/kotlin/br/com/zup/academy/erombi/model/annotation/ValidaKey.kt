package br.com.zup.academy.erombi.model.annotation

import br.com.zup.academy.erombi.TipoKey
import br.com.zup.academy.erombi.service.form.NovaKeyForm
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import jakarta.inject.Singleton
import javax.validation.Constraint
import kotlin.reflect.KClass


@MustBeDocumented
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidaKeyValidator::class])
annotation class ValidaKey (
    val message: String = "Key inválida !",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Any>> = [],
)

@Singleton
class ValidaKeyValidator()
    : ConstraintValidator<ValidaKey, NovaKeyForm> {

    override fun isValid(value: NovaKeyForm?,
                         annotationMetadata: AnnotationValue<ValidaKey>,
                         context: ConstraintValidatorContext): Boolean {

        return value?.key?.let { key ->
            value.tipoKey?.validaKey(key)
        } ?: true

    }

}

fun TipoKey.validaKey(key: String): Boolean {
    when (this){

        TipoKey.UNKNOWN_TIPO_KEY -> {
            return false
        }
        TipoKey.CPF -> {
            return key.matches("^[0-9]{11}\$".toRegex())
        }
        TipoKey.CNPJ -> {
            return key.matches("^\\d{2}\\.\\d{3}\\.\\d{3}\\/\\d{4}\\-\\d{2}\$".toRegex())
        }
        TipoKey.PHONE -> {
            return key.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
        TipoKey.EMAIL -> {
            return key.matches("[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?".toRegex())
        }
        TipoKey.RANDOM -> {
            return key.isEmpty()
        }

        else -> return false
    }
}