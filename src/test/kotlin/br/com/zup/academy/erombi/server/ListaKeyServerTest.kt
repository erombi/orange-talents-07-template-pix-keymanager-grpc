package br.com.zup.academy.erombi.server

import br.com.zup.academy.erombi.ConsultaKeyPorClienteRequest
import br.com.zup.academy.erombi.KeyManagerGrpcServiceGrpc
import br.com.zup.academy.erombi.TipoConta
import br.com.zup.academy.erombi.TipoKey
import br.com.zup.academy.erombi.client.ErpItauClient
import br.com.zup.academy.erombi.client.response.ClienteErpItauResponse
import br.com.zup.academy.erombi.client.response.InstituicaoErpItauResponse
import br.com.zup.academy.erombi.model.Instituicao
import br.com.zup.academy.erombi.model.Key
import br.com.zup.academy.erombi.model.Titular
import br.com.zup.academy.erombi.repository.KeyRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*

@MicronautTest(transactional = false)
internal class ListaKeyServerTest(
    val grpcClient : KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub,
    val repository: KeyRepository
) {

    @Inject
    lateinit var erpItauClient: ErpItauClient

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @AfterEach
    fun after() {
        repository.deleteAll()
    }

    @MockBean(ErpItauClient::class)
    fun erpItau(): ErpItauClient? {
        return Mockito.mock(ErpItauClient::class.java)
    }

    @Test
    fun `deve listar keys com sucesso`() {
        val idCliente = UUID.randomUUID()
        `when`(
            erpItauClient.pesquisaCliente(idCliente.toString())
        ).thenReturn(
            ClienteErpItauResponse(
                UUID.fromString(idCliente.toString()),
                "Leonardo Silva",
                "40764442058",
                InstituicaoErpItauResponse(
                    "ITAÚ UNIBANCO S.A.",
                    "60701190"
                )
            )
        )

        val keySalva = repository.save(
            Key(
                TipoKey.CPF,
                "40764442058",
                TipoConta.CACC,
                Instituicao(
                    100000,
                    "ITAÚ UNIBANCO S.A.",
                    "60701190"
                ),
                "0338",
                "123564",
                Titular(
                    idCliente.toString(),
                    "Leonardo Silva",
                    "40764442058"
                )
            )
        )

        val response = grpcClient.consultaKeysPorCliente(
            ConsultaKeyPorClienteRequest.newBuilder()
                    .setIdCliente(idCliente.toString())
                .build()
        )

        Assertions.assertEquals(idCliente.toString(), response.keysList[0].clienteId)
        Assertions.assertEquals(keySalva.id.toString(), response.keysList[0].pixId)
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT`() {
        val idCliente = UUID.randomUUID()
        `when`(
            erpItauClient.pesquisaCliente(idCliente.toString())
        ).thenReturn(null)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consultaKeysPorCliente(
                ConsultaKeyPorClienteRequest.newBuilder()
                    .setIdCliente(idCliente.toString())
                    .build()
            )
        }

        Assertions.assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)

    }

    @Test
    fun `deve listar nenhuma key`() {
        val idCliente = UUID.randomUUID()
        `when`(
            erpItauClient.pesquisaCliente(idCliente.toString())
        ).thenReturn(
            ClienteErpItauResponse(
                UUID.fromString(idCliente.toString()),
                "Leonardo Silva",
                "40764442058",
                InstituicaoErpItauResponse(
                    "ITAÚ UNIBANCO S.A.",
                    "60701190"
                )
            )
        )

        val response = grpcClient.consultaKeysPorCliente(
            ConsultaKeyPorClienteRequest.newBuilder()
                .setIdCliente(idCliente.toString())
                .build()
        )

        Assertions.assertTrue(response.keysList.isEmpty())
    }
}
