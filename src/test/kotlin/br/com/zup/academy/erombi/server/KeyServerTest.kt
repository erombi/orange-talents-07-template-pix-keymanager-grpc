package br.com.zup.academy.erombi.server

import br.com.zup.academy.erombi.*
import br.com.zup.academy.erombi.repository.KeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class KeyServerTest(
    val client: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub,
    val repository: KeyRepository
) {

    @Test
    fun `deve cadastrar uma Key com sucesso`() {
        repository.deleteAll()

        val response = client.cadastrarKey(
            NovaKeyRequest.newBuilder()
                .setUuidCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                .setTipoKey(TipoKey.PHONE)
                .setKey("+5519999741000")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

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
                    .setKey("+5519999741000")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(error) {
            Assertions.assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            Assertions.assertEquals("Cliente inexistente !", status.description)
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
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        val error = assertThrows<StatusRuntimeException> {
            client.cadastrarKey(
                NovaKeyRequest.newBuilder()
                    .setUuidCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                    .setTipoKey(TipoKey.PHONE)
                    .setKey("+5519999741000")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(error) {
            Assertions.assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            Assertions.assertEquals("Key já existente !", status.description)
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
                    .setTipoConta(TipoConta.CONTA_POUPANCA)
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
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        Assertions.assertTrue(repository.existsByIdAndTitularUuidCliente(UUID.fromString(responseCadastro.pixId), "ae93a61c-0642-43b3-bb8e-a17072295955"))

        val response = client.removeKey(
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

        val responseCadastro = client.cadastrarKey(
            NovaKeyRequest.newBuilder()
                .setUuidCliente("ae93a61c-0642-43b3-bb8e-a17072295955")
                .setTipoKey(TipoKey.PHONE)
                .setKey("+5519999741000")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        Assertions.assertTrue(repository.existsByIdAndTitularUuidCliente(UUID.fromString(responseCadastro.pixId), "ae93a61c-0642-43b3-bb8e-a17072295955"))

        val error = assertThrows<StatusRuntimeException> {
            client.removeKey(
                RemoveKeyRequest.newBuilder()
                    .setIdKey(responseCadastro.pixId)
                    .setIdCliente("-0642-43b3-bb8e-a17072295955")
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
            Assertions.assertEquals("NOT_FOUND: Key não encontrada !", status.description)
        }
    }
}

@Factory
class KeyServerClientFactory {

    @Singleton
    fun carrosClientGrpc(@GrpcChannel(GrpcServerChannel.NAME) channel : ManagedChannel) : KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub {
        return KeyManagerGrpcServiceGrpc.newBlockingStub(channel);
    }

}