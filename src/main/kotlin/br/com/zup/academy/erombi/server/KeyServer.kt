package br.com.zup.academy.erombi.server

import br.com.zup.academy.erombi.KeyManagerGrpcServiceGrpc
import br.com.zup.academy.erombi.NovaKeyRequest
import br.com.zup.academy.erombi.NovaKeyResponse
import br.com.zup.academy.erombi.client.ErpItauClient
import br.com.zup.academy.erombi.repository.KeyRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class KeyServer(
    val repository: KeyRepository,
    val erpItauClient: ErpItauClient
) : KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    val logger: Logger = LoggerFactory.getLogger(KeyServer::class.java)

    override fun cadastrarKey(request: NovaKeyRequest?, responseObserver: StreamObserver<NovaKeyResponse>?) {
        if (request?.uuidCliente == null || request.key == null ||
            request.tipoKey == null || request.tipoConta == null) {

            responseObserver?.onError(Status.INVALID_ARGUMENT
                                            .withDescription("Campo obrigatório não pode ser nulo !")
                                            .asRuntimeException())
            return
        }

        logger.info("Iniciando consulta de cliente")

        try {
            val response = erpItauClient.pesquisaCliente(request.uuidCliente)

            logger.info("Consulta de cliente realizada")



            responseObserver?.onNext(
                NovaKeyResponse.newBuilder()
                    .setPixId("")
                    .build()
            )
            responseObserver?.onCompleted()

        } catch (e : HttpClientResponseException) {
            if (e.status == HttpStatus.BAD_REQUEST) {
                logger.warn("Erro na consulta de cliente, não encontrado")
                responseObserver?.onError(Status.NOT_FOUND
                        .withDescription("Cliente não encontrado !")
                        .asRuntimeException())
                return
            } else {
                logger.warn("Erro inesperado na consulta de cliente")
                responseObserver?.onError(Status.INTERNAL
                    .withDescription("Erro inesperado !")
                    .asRuntimeException())
                return
            }
        }
    }
}