package br.com.zup.academy.erombi.model.annotation

import br.com.zup.academy.erombi.client.ErpItauClient
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Constraint
import kotlin.reflect.KClass


@MustBeDocumented
@Target(AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidaClienteValidator::class])
annotation class ValidaCliente (
    val message: String = "Cliente inexistente !",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Any>> = [],
)

@Singleton
class ValidaClienteValidator(
    val client: ErpItauClient
) : ConstraintValidator<ValidaCliente, String> {

    val logger: Logger = LoggerFactory.getLogger(ValidaClienteValidator::class.java)

    override fun isValid(value: String?,
                         annotationMetadata: AnnotationValue<ValidaCliente>,
                         context: ConstraintValidatorContext): Boolean {
        return try {
            value?.let { uuidCliente ->
                client.pesquisaCliente(uuidCliente)
            }

            true
        } catch (e : HttpClientResponseException) {
            if (e.status == HttpStatus.BAD_REQUEST) logger.warn("Erro na consulta de cliente, não encontrado")
            else logger.warn("Erro inesperado na consulta de cliente")

            false
        }
    }

}