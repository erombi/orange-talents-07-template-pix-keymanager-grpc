package br.com.zup.academy.erombi.service

import br.com.zup.academy.erombi.NovaKeyResponse
import br.com.zup.academy.erombi.RemoveKeyResponse
import br.com.zup.academy.erombi.TipoConta
import br.com.zup.academy.erombi.client.BcbClient
import br.com.zup.academy.erombi.client.ErpItauClient
import br.com.zup.academy.erombi.client.request.BankAccountRequest
import br.com.zup.academy.erombi.client.request.CreatePixKeyRequest
import br.com.zup.academy.erombi.client.request.DeletePixKeyRequest
import br.com.zup.academy.erombi.client.request.OwnerRequest
import br.com.zup.academy.erombi.model.Instituicao
import br.com.zup.academy.erombi.model.Key
import br.com.zup.academy.erombi.model.TipoPessoa
import br.com.zup.academy.erombi.model.Titular
import br.com.zup.academy.erombi.repository.KeyRepository
import br.com.zup.academy.erombi.service.form.NovaKeyForm
import br.com.zup.academy.erombi.service.form.RemoveKeyForm
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import io.micronaut.validation.validator.Validator
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.Valid
import kotlin.random.Random

@Validated
@Singleton
class KeyService(
    val client: ErpItauClient,
    val repository: KeyRepository,
    val validator: Validator,
    val clientBcb: BcbClient
) {

    private val logger: Logger = LoggerFactory.getLogger(KeyService::class.java)

    fun validaESalva(@Valid form : NovaKeyForm): NovaKeyResponse {
        try {
            val contaClienteItau = client.pesquisaContasPorCliente(form.tipoConta!!.toPorExtenso(), form.uuidCliente!!)

            contaClienteItau?.let {

                val response = clientBcb.cadastraNoBancoCentral(
                    CreatePixKeyRequest(
                        form.tipoKey!!.name,
                        form.key!!,
                        BankAccountRequest(
                            100000,
                            "0001",
                            100000,
                            form.tipoConta.name
                        ),
                        OwnerRequest(
                            TipoPessoa.NATURAL_PERSON.name,
                            contaClienteItau.titular.nome,
                            contaClienteItau.titular.cpf
                        )
                    )
                )

                val key = Key(
                    form.tipoKey,
                    response.key,
                    form.tipoConta,
                    Instituicao(
                        response.bankAccount.participant,
                        contaClienteItau.instituicao.nome,
                        contaClienteItau.instituicao.ispb
                    ),
                    contaClienteItau.agencia,
                    contaClienteItau.numero,
                    Titular(
                        contaClienteItau.titular.id,
                        contaClienteItau.titular.nome,
                        contaClienteItau.titular.cpf
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
            if (e.response.status == HttpStatus.UNPROCESSABLE_ENTITY) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Chave já cadastrada no Banco Central")
                    .asRuntimeException()
            }

            logger.error("Ocorreu um erro inesperado")
            throw StatusRuntimeException(Status.INTERNAL.withDescription("Ocorreu um erro inesperado !"))
        } catch (e: Exception) {
            throw e
        }

    }

    fun validaERemove(form: RemoveKeyForm): RemoveKeyResponse {
        val errors = validator.validate(form)

        if (errors.isNotEmpty()) {
            val primeiroErro = errors.first()
            with(primeiroErro) {
                if (message == "Key não encontrada ou em formato inválido !")
                    throw StatusRuntimeException(Status.NOT_FOUND.withDescription(message))

                if (message == "Cliente inexistente !")
                    throw StatusRuntimeException(Status.NOT_FOUND.withDescription(message))

                throw StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(message))
            }
        }

        val key = repository.findById(UUID.fromString(form.idKey)).get()

        try {
            val response = clientBcb.deletaDoBancoCentral(key.key,
                DeletePixKeyRequest(
                    key.key,
                    key.instituicao.participant
                )
            )

            repository.deleteByIdAndTitularUuidCliente(UUID.fromString(form.idKey), form.idCliente)
            return RemoveKeyResponse.newBuilder().build()

        } catch (e: HttpClientResponseException) {
            logger.warn("Não foi possivel deletar key do banco central")
            throw StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Não foi possível deletar a chave !"))
        }
    }

}

fun TipoConta.toPorExtenso(): String {
    return when(this) {
        (TipoConta.UNKNOWN_TIPO_CONTA) -> "UNKNOWN_TIPO_CONTA"
        (TipoConta.CACC) -> "CONTA_CORRENTE"
        (TipoConta.SVGS) -> "CONTA_POUPANCA"

        else -> ""
    }
}