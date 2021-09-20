package br.com.zup.academy.erombi.service

import br.com.zup.academy.erombi.NovaKeyResponse
import br.com.zup.academy.erombi.RemoveKeyResponse
import br.com.zup.academy.erombi.client.ErpItauClient
import br.com.zup.academy.erombi.model.Instituicao
import br.com.zup.academy.erombi.model.Key
import br.com.zup.academy.erombi.model.Titular
import br.com.zup.academy.erombi.repository.KeyRepository
import br.com.zup.academy.erombi.service.form.NovaKeyForm
import br.com.zup.academy.erombi.service.form.RemoveKeyForm
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import io.micronaut.validation.validator.Validator
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.Valid

@Validated
@Singleton
class KeyService(
    val client: ErpItauClient,
    val repository: KeyRepository,
    val validator: Validator
) {

    private val logger: Logger = LoggerFactory.getLogger(KeyService::class.java)

    fun validaESalva(@Valid form : NovaKeyForm): NovaKeyResponse {
        try {
            val contaClienteItau = client.pesquisaContasPorCliente(form.tipoConta!!.name, form.uuidCliente!!)

            contaClienteItau?.let {
                val key = Key(
                    form.tipoKey!!,
                    form.key!!,
                    form.tipoConta,
                    Instituicao(
                        contaClienteItau.instituicao.nome,
                        contaClienteItau.instituicao.ispb
                    ),
                    contaClienteItau.agencia,
                    contaClienteItau.numero,
                    Titular(
                        contaClienteItau.titular.id,
                        contaClienteItau.titular.nome
                    )
                )

                repository.save(key)

                return NovaKeyResponse.newBuilder()
                    .setPixId(key.id.toString())
                    .build()
            } ?: throw StatusRuntimeException(
                Status.INVALID_ARGUMENT
                        .withDescription("Conta não encontrada !")
            )


        } catch (e : HttpClientResponseException) {
            logger.error("Ocorreu um erro inesperado na consulta de conta")
            throw StatusRuntimeException(Status.INTERNAL.withDescription("Ocorreu um erro inesperado !"))
        }

    }

    fun validaERemove(form: RemoveKeyForm): RemoveKeyResponse {
        val errors = validator.validate(form)

        if (errors.isNotEmpty()) {
            val primeiroErro = errors.first()
            with(primeiroErro) {
                if (message == "Key não encontrada !")
                    throw StatusRuntimeException(Status.NOT_FOUND.withDescription(message))

                if (message == "Cliente inexistente !")
                    throw StatusRuntimeException(Status.NOT_FOUND.withDescription(message))

                throw StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(message))
            }
        }

        repository.deleteByIdAndTitularUuidCliente(UUID.fromString(form.idKey), form.idCliente)
        return RemoveKeyResponse.newBuilder().build()
    }

}