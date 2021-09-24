package br.com.zup.academy.erombi.server

import br.com.zup.academy.erombi.*
import br.com.zup.academy.erombi.client.BcbClient
import br.com.zup.academy.erombi.client.ErpItauClient
import br.com.zup.academy.erombi.client.request.BankAccountRequest
import br.com.zup.academy.erombi.client.request.CreatePixKeyRequest
import br.com.zup.academy.erombi.client.request.DeletePixKeyRequest
import br.com.zup.academy.erombi.client.request.OwnerRequest
import br.com.zup.academy.erombi.client.response.*
import br.com.zup.academy.erombi.model.TipoPessoa
import br.com.zup.academy.erombi.repository.KeyRepository
import br.com.zup.academy.erombi.service.toPorExtenso
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import java.time.LocalDateTime
import java.util.*
import javax.validation.ConstraintViolationException

@MicronautTest(transactional = false)
internal class KeyServerTest(
    val client: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub,
    val repository: KeyRepository
) {

    @Inject
    lateinit var clientErpItau: ErpItauClient

    @Inject
    lateinit var clientBcb: BcbClient

    @BeforeEach
    fun setUp() {
        `when`(clientErpItau.pesquisaContasPorCliente(TipoConta.CACC.toPorExtenso(), "0642-43b3-bb8e-a17072295955"))
            .thenThrow(
                ConstraintViolationException(mutableSetOf())
            )

        `when`(clientErpItau.pesquisaContasPorCliente(TipoConta.CACC.toPorExtenso(), "ae93a61c-0642-43b3-bb8e-a17072295955"))
            .thenReturn(
                ContaClienteErpItauResponse(
                    "CONTA_CORRENTE",
                    InstituicaoErpItauResponse(
                        "ITAÚ UNIBANCO S.A.",
                        "60701190"
                    ),
                    "0001",
                    "125987",
                    TitularErpItauResponse(
                        "ae93a61c-0642-43b3-bb8e-a17072295955",
                        "Leonardo Silva",
                        "40764442058"
                    ),
                )
            )

        `when`(
            clientErpItau.pesquisaCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
        ).thenReturn(
            ClienteErpItauResponse(
                UUID.fromString("ae93a61c-0642-43b3-bb8e-a17072295955"),
                "Leonardo Silva",
                "40764442058",
                InstituicaoErpItauResponse(
                    "ITAÚ UNIBANCO S.A.",
                    "60701190"
                )
            )
        )

        `when`(clientBcb.cadastraNoBancoCentral(
            CreatePixKeyRequest(
                TipoKey.PHONE.name,
                "+5519999741000",
                BankAccountRequest(
                    100000,
                    "0001",
                    100000,
                    TipoConta.CACC.name
                ),
                OwnerRequest(
                    TipoPessoa.NATURAL_PERSON.name,
                    "Leonardo Silva",
                    "40764442058"
                )
            )
        )).thenReturn(
            CreatePixKeyResponse(
                TipoKey.PHONE,
                "+5519999741000",
                BankAccountRequest(
                    100000,
                    "0001",
                    100000,
                    TipoConta.CACC.name
                ),
                OwnerRequest(
                    TipoPessoa.NATURAL_PERSON.name,
                    "Leonardo Silva",
                    "40764442058"
                ),
                LocalDateTime.now()
            )
        )
    }

    @Test
    fun `deve cadastrar uma Key com sucesso`() {
        repository.deleteAll()


        val response = client.cadastrarKey(
            NovaKeyRequest.newBuilder()
                .setUuidCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                .setTipoKey(TipoKey.PHONE)
                .setKey("+5519999741000")
                .setTipoConta(TipoConta.CACC)
                .build()
        )

        Mockito.verify(clientErpItau, times(1)).pesquisaContasPorCliente("CONTA_CORRENTE", "ae93a61c-0642-43b3-bb8e-a17072295955")

        Assertions.assertTrue(response.pixId.isNotBlank())

        val possivelKey = repository.findById(UUID.fromString(response.pixId))

        Assertions.assertTrue(possivelKey.isPresent)

        val key = possivelKey.get()
        Assertions.assertEquals(response.pixId, key.id.toString())
    }

    @Test
    fun `deve retornar erro de cliente nao encontrado`() {
        repository.deleteAll()

        val error = assertThrows<StatusRuntimeException> {
            client.cadastrarKey(
                NovaKeyRequest.newBuilder()
                    .setUuidCliente("0642-43b3-bb8e-a17072295955")
                    .setTipoKey(TipoKey.PHONE)
                    .setKey("+5519999742222")
                    .setTipoConta(TipoConta.CACC)
                    .build()
            )
        }

        with(error) {
            Assertions.assertEquals(Status.FAILED_PRECONDITION.code, status.code)
        }
    }

    @Test
    fun `deve retornar erro de key unica`() {
        repository.deleteAll()

        client.cadastrarKey(
            NovaKeyRequest.newBuilder()
                .setUuidCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                .setTipoKey(TipoKey.PHONE)
                .setKey("+5519999741000")
                .setTipoConta(TipoConta.CACC)
                .build()
        )

        val error = assertThrows<StatusRuntimeException> {
            client.cadastrarKey(
                NovaKeyRequest.newBuilder()
                    .setUuidCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                    .setTipoKey(TipoKey.PHONE)
                    .setKey("+5519999741000")
                    .setTipoConta(TipoConta.CACC)
                    .build()
            )
        }

        with(error) {
            Assertions.assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            Assertions.assertEquals("validaESalva.form.key: Key já existente !", status.description)
        }

        val existente = repository.findById(UUID.fromString("aaaaa61c-0642-43b3-bb8e-a17072295955"))

        Assertions.assertTrue(existente.isEmpty)
    }

    @Test
    fun `deve retornar erro de conta nao encontrada`() {
        repository.deleteAll()

        val error = assertThrows<StatusRuntimeException> {
            client.cadastrarKey(
                NovaKeyRequest.newBuilder()
                    .setUuidCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoKey(TipoKey.PHONE)
                    .setKey("+5519999741000")
                    .setTipoConta(TipoConta.SVGS)
                    .build()
            )
        }

        with(error) {
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            Assertions.assertEquals("INVALID_ARGUMENT: Conta não encontrada !", status.description)
        }

        val existente = repository.findById(UUID.fromString("ae93a61c-0642-43b3-bb8e-a17072295955"))

        Assertions.assertTrue(existente.isEmpty)
    }

    @Test
    fun  `deve remover Key com sucesso`() {
        repository.deleteAll()

        val responseCadastro = client.cadastrarKey(
            NovaKeyRequest.newBuilder()
                .setUuidCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                .setTipoKey(TipoKey.PHONE)
                .setKey("+5519999741000")
                .setTipoConta(TipoConta.CACC)
                .build()
        )

        Assertions.assertTrue(repository.existsByIdAndTitularUuidCliente(UUID.fromString(responseCadastro.pixId), "ae93a61c-0642-43b3-bb8e-a17072295955"))

        client.removeKey(
            RemoveKeyRequest.newBuilder()
                .setIdKey(responseCadastro.pixId)
                .setIdCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                .build()
        )

        Assertions.assertFalse(repository.existsByIdAndTitularUuidCliente(UUID.fromString(responseCadastro.pixId), "ae93a61c-0642-43b3-bb8e-a17072295955"))
    }

    @Test
    fun `deve nao remover Key quando cliente invalido`() {
        repository.deleteAll()

        val clienteinexistente = UUID.randomUUID()

        `when`(clientErpItau.pesquisaContasPorCliente(TipoConta.CACC.toPorExtenso(), clienteinexistente.toString()))
            .thenThrow(
                ConstraintViolationException("Cliente inexistente !", mutableSetOf())
            )

        val responseCadastro = client.cadastrarKey(
            NovaKeyRequest.newBuilder()
                .setUuidCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                .setTipoKey(TipoKey.PHONE)
                .setKey("+5519999741000")
                .setTipoConta(TipoConta.CACC)
                .build()
        )

        val error = assertThrows<StatusRuntimeException> {
            client.removeKey(
                RemoveKeyRequest.newBuilder()
                    .setIdKey(responseCadastro.pixId)
                    .setIdCliente(clienteinexistente.toString())
                    .build()
            )
        }

        /* Como a validação da key inclui o id do cliente não tem como testar unicamente essa validação */
        with(error) {
            Assertions.assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Test
    fun `deve nao remover Key quando key inexistente`() {
        repository.deleteAll()

        val error = assertThrows<StatusRuntimeException> {
            client.removeKey(
                RemoveKeyRequest.newBuilder()
                    .setIdKey(UUID.randomUUID().toString())
                    .setIdCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                    .build()
            )
        }

        with(error) {
            Assertions.assertEquals(Status.NOT_FOUND.code, status.code)
            Assertions.assertEquals("NOT_FOUND: Key não encontrada ou em formato inválido !", status.description)
        }
    }

    @Test
    fun `deve retornar UNAVAILABLE quando servico fora do ar`() {
        repository.deleteAll()

        val randomUUID = UUID.randomUUID()

        `when`(clientErpItau.pesquisaContasPorCliente(TipoConta.CACC.toPorExtenso(), randomUUID.toString()))
            .thenThrow(
                RuntimeException("Connection refused", HttpClientException("Connection refused"))
            )

        val error = assertThrows<StatusRuntimeException> {
            client.cadastrarKey(
                NovaKeyRequest.newBuilder()
                    .setUuidCliente(randomUUID.toString())
                    .setTipoKey(TipoKey.PHONE)
                    .setKey("+5519999741111")
                    .setTipoConta(TipoConta.CACC)
                    .build()
            )
        }

        Assertions.assertEquals(Status.UNAVAILABLE.code, error.status.code)
        Assertions.assertEquals("Erro ao conectar à serviço externo !", error.status.description)
    }

    @Test
    fun `deve retornar INTERNAL erro inesperado`() {
        repository.deleteAll()

        val randomUUID = UUID.randomUUID()

        `when`(clientErpItau.pesquisaContasPorCliente(TipoConta.CACC.toPorExtenso(), randomUUID.toString()))
            .thenThrow(
                RuntimeException("Connection refused")
            )

        val error = assertThrows<StatusRuntimeException> {
            client.cadastrarKey(
                NovaKeyRequest.newBuilder()
                    .setUuidCliente(randomUUID.toString())
                    .setTipoKey(TipoKey.PHONE)
                    .setKey("+5519999741000")
                    .setTipoConta(TipoConta.CACC)
                    .build()
            )
        }

        Assertions.assertEquals(Status.INTERNAL.code, error.status.code)
        Assertions.assertEquals("Erro interno inesperado !", error.status.description)
    }

    @Test
    fun `deve retornar UNAVAILABLE quando servico fora do ar ao deletar`() {
        repository.deleteAll()

        val response = client.cadastrarKey(
            NovaKeyRequest.newBuilder()
                .setUuidCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                .setTipoKey(TipoKey.PHONE)
                .setKey("+5519999741000")
                .setTipoConta(TipoConta.CACC)
                .build()
        )

        `when`(clientBcb.deletaDoBancoCentral(
            "+5519999741000",
            DeletePixKeyRequest(
                "+5519999741000",
                100000
            )
        ))
            .thenThrow(
                RuntimeException("Connection refused", HttpClientException("Connection refused"))
            )

        val error = assertThrows<StatusRuntimeException> {
            client.removeKey(
                RemoveKeyRequest.newBuilder()
                    .setIdKey(response.pixId)
                    .setIdCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                    .build()
            )
        }

        Assertions.assertEquals(Status.UNAVAILABLE.code, error.status.code)
        Assertions.assertEquals("Erro ao conectar à serviço externo !", error.status.description)
    }

    @Test
    fun `deve retornar INTERNAL erro inesperado ao deletar`() {
        repository.deleteAll()

        val response = client.cadastrarKey(
            NovaKeyRequest.newBuilder()
                .setUuidCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                .setTipoKey(TipoKey.PHONE)
                .setKey("+5519999741000")
                .setTipoConta(TipoConta.CACC)
                .build()
        )

        `when`(clientBcb.deletaDoBancoCentral(
            "+5519999741000",
            DeletePixKeyRequest(
                "+5519999741000",
                100000
            )
        ))
            .thenThrow(
                RuntimeException("Connection refused")
            )

        val error = assertThrows<StatusRuntimeException> {
            client.removeKey(
                RemoveKeyRequest.newBuilder()
                    .setIdKey(response.pixId)
                    .setIdCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                    .build()
            )
        }

        Assertions.assertEquals(Status.INTERNAL.code, error.status.code)
        Assertions.assertEquals("Erro interno inesperado !", error.status.description)
    }

    @Test
    fun `deve lancar Exception de httpClientResponseException`() {
        repository.deleteAll()

        val response = client.cadastrarKey(
            NovaKeyRequest.newBuilder()
                .setUuidCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                .setTipoKey(TipoKey.PHONE)
                .setKey("+5519999741000")
                .setTipoConta(TipoConta.CACC)
                .build()
        )

        `when`(clientBcb.deletaDoBancoCentral(
            "+5519999741000",
            DeletePixKeyRequest(
                "+5519999741000",
                100000
            )
        ))
            .thenThrow(HttpClientResponseException::class.java)

        val error = assertThrows<StatusRuntimeException> {
            client.removeKey(
                RemoveKeyRequest.newBuilder()
                    .setIdKey(response.pixId)
                    .setIdCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                    .build()
            )
        }

        Assertions.assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        Assertions.assertEquals("INVALID_ARGUMENT: Não foi possível deletar a chave !", error.status.description)
    }

    @MockBean(ErpItauClient::class)
    fun erpItau(): ErpItauClient? {
        return Mockito.mock(ErpItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bancoCentral(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class KeyServerClientFactory {

        @Singleton
        fun carrosClientGrpc(@GrpcChannel(GrpcServerChannel.NAME) channel : ManagedChannel) : KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub {
            return KeyManagerGrpcServiceGrpc.newBlockingStub(channel);
        }

    }
}

