package br.com.zup.academy.erombi.server

import br.com.zup.academy.erombi.ConsultaKeyRequest
import br.com.zup.academy.erombi.KeyManagerGrpcServiceGrpc
import br.com.zup.academy.erombi.TipoConta
import br.com.zup.academy.erombi.TipoKey
import br.com.zup.academy.erombi.client.BcbClient
import br.com.zup.academy.erombi.client.request.BankAccountRequest
import br.com.zup.academy.erombi.client.request.OwnerRequest
import br.com.zup.academy.erombi.client.response.PixKeyDetailsResponse
import br.com.zup.academy.erombi.model.Instituicao
import br.com.zup.academy.erombi.model.Key
import br.com.zup.academy.erombi.model.TipoPessoa
import br.com.zup.academy.erombi.model.Titular
import br.com.zup.academy.erombi.repository.KeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class ConsultaKeyServerTest(
    val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub,
    val repository: KeyRepository
) {

    @Inject
    lateinit var bcbClient: BcbClient

    lateinit var pixIdDefault: String

    @BeforeEach
    fun setUp() {
        repository.deleteAll()

        val key = repository.save(
            Key(
                TipoKey.EMAIL,
                "eduardo@zup.com",
                TipoConta.CACC,
                Instituicao(
                    100000,
                    "ITAÚ UNIBANCO S.A.",
                    "60701190"
                ),
                "0001",
                "125987",
                Titular(
                    "91cce9ae-74e4-4c18-96e9-50d941f2f6ff",
                    "Eduardo",
                    "11111111111"
                )
            )
        )

        pixIdDefault = key.id.toString()
    }

    @AfterEach
    fun finally() {
        repository.deleteAll()
    }

    @MockBean(BcbClient::class)
    fun bancoCentral(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }



    @Test
    fun `deve consultar por key com sucesso`() {

        val response = grpcClient.consultaKey(
            ConsultaKeyRequest.newBuilder()
                .setKey("eduardo@zup.com")
                .build()
        )

        Assertions.assertEquals("eduardo@zup.com", response.chave.key)
    }

    @Test
    fun `deve consultar por Key e batendo no banco central`() {
        val key = "48243048812";

        `when`(
            bcbClient.consultaKey(key)
        ).thenReturn(
            HttpResponse.ok(
                PixKeyDetailsResponse(
                    TipoKey.CPF,
                    key,
                    BankAccountRequest(100000,"0001", 213456, TipoConta.CACC.name),
                    OwnerRequest(TipoPessoa.NATURAL_PERSON.name, "Jorge", key),
                    LocalDateTime.now()
                )
            )
        )

        val response = grpcClient.consultaKey(
            ConsultaKeyRequest.newBuilder()
                .setKey(key)
                .build()
        )

        Assertions.assertEquals("Jorge", response.chave.conta.nomeTitular)
        Assertions.assertEquals(key, response.chave.key)
        Assertions.assertEquals(key, response.chave.conta.cpfTitular)

    }

    @Test
    fun `deve consultar por Key e lancar NOT_FOUND`() {
        val key = "48243048812";

        `when`(
            bcbClient.consultaKey(key)
        ).thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consultaKey(
                ConsultaKeyRequest.newBuilder()
                    .setKey(key)
                    .build()
            )
        }

        with(error) {
            Assertions.assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Test
    fun `deve consultar por Pix`() {

        val response = grpcClient.consultaKey(
            ConsultaKeyRequest.newBuilder()
                .setPix(
                    ConsultaKeyRequest.FiltroPorPix.newBuilder()
                            .setIdKey(pixIdDefault)
                            .setIdCliente("91cce9ae-74e4-4c18-96e9-50d941f2f6ff")
                        .build()
                )
                .build()
        )

        Assertions.assertEquals("Eduardo", response.chave.conta.nomeTitular)
        Assertions.assertEquals("eduardo@zup.com", response.chave.key)
        Assertions.assertEquals("11111111111", response.chave.conta.cpfTitular)
    }

    @Test
    fun `deve consultar por Pix e lancar NOT_FOUND`() {
        val idInvalido = UUID.randomUUID().toString()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consultaKey(
                ConsultaKeyRequest.newBuilder()
                    .setPix(
                        ConsultaKeyRequest.FiltroPorPix.newBuilder()
                            .setIdKey(idInvalido)
                            .setIdCliente(idInvalido)
                            .build()
                    )
                    .build()
            )
        }

        with(error) {
            Assertions.assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Test
    fun `deve consultar por Pix e lancar INVALID_ARGUMENT`() {
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consultaKey(
                ConsultaKeyRequest.newBuilder()
                    .setPix(
                        ConsultaKeyRequest.FiltroPorPix.newBuilder()
                            .setIdKey("invalido")
                            .setIdCliente("invalido")
                            .build()
                    )
                    .build()
            )
        }

        with(error) {
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }
}

