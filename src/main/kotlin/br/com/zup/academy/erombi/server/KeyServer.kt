package br.com.zup.academy.erombi.server

import br.com.zup.academy.erombi.*
import br.com.zup.academy.erombi.client.BcbClient
import br.com.zup.academy.erombi.compartilhado.toModel
import br.com.zup.academy.erombi.exception.NotFoundException
import br.com.zup.academy.erombi.repository.KeyRepository
import br.com.zup.academy.erombi.service.KeyService
import br.com.zup.academy.erombi.service.form.ConsultaKeyPorClienteForm
import br.com.zup.academy.erombi.service.form.NovaKeyForm
import br.com.zup.academy.erombi.service.form.RemoveKeyForm
import com.google.protobuf.Any
import com.google.rpc.BadRequest
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.validation.Validated
import io.micronaut.validation.validator.Validator
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException

@Validated
@Singleton
class KeyServer(
    val service: KeyService,
    val validator: Validator,
    val repository: KeyRepository,
    val bcbClient: BcbClient
) : KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    val logger: Logger = LoggerFactory.getLogger(KeyServer::class.java)

    override fun cadastrarKey(request: NovaKeyRequest?, responseObserver: StreamObserver<NovaKeyResponse>?) {

        try {
            val response = request?.let { req ->
                val form = with(req) {
                    NovaKeyForm(
                        uuidCliente,
                        tipoKey,
                        key,
                        tipoConta,
                    )
                }
                service.validaESalva(form)
            }

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()
        } catch (e: StatusRuntimeException) {
            responseObserver?.onError(Status.INVALID_ARGUMENT
                                                .withDescription(e.message)
                                                .asRuntimeException())
        } catch (e: ConstraintViolationException) {
            responseObserver?.onError(Status.FAILED_PRECONDITION
                                                .withDescription(e.message)
                                                .asRuntimeException())
        } catch (e: Exception) {
            if (e.cause is HttpClientException){
                responseObserver?.onError(Status.UNAVAILABLE
                        .withDescription("Erro ao conectar à serviço externo !")
                    .asRuntimeException())
            }

            responseObserver?.onError(Status.INTERNAL
                .withDescription("Erro interno inesperado !")
                .asRuntimeException())
        }
    }

    override fun removeKey(request: RemoveKeyRequest?, responseObserver: StreamObserver<RemoveKeyResponse>?) {
        try {
            val response = request?.let { req ->
                val form = with(req) {
                    RemoveKeyForm(
                        req.idKey,
                        req.idCliente
                    )
                }

                service.validaERemove(form)
            }

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()

        } catch (e: StatusRuntimeException) {
            responseObserver?.onError(e.status.withDescription(e.message).asRuntimeException())

        } catch (e: Exception) {
            if (e.cause is HttpClientException){
                responseObserver?.onError(Status.UNAVAILABLE
                    .withDescription("Erro ao conectar à serviço externo !")
                    .asRuntimeException())
            }

            responseObserver?.onError(Status.INTERNAL
                .withDescription("Erro interno inesperado !")
                .asRuntimeException())
        }
    }

    override fun consultaKey(request: ConsultaKeyRequest, responseObserver: StreamObserver<ConsultaKeyResponse>?) {
        try {
            val filtro = request.toModel(validator)
            val response = filtro.filtra(repository, bcbClient)

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()
        } catch (e: NotFoundException) {
            responseObserver?.onError(Status.NOT_FOUND.withDescription(e.message).asRuntimeException())
        } catch (e: Exception) {
            responseObserver?.onError(Status.INVALID_ARGUMENT.withDescription(e.message).asRuntimeException())
        }

    }

    override fun consultaKeysPorCliente(
        request: ConsultaKeyPorClienteRequest,
        responseObserver: StreamObserver<ConsultaKeyPorClienteResponse>?
    ) {
        try {
            val keys = service.validaEConsultaPorCliente(
                ConsultaKeyPorClienteForm(request.idCliente)
            )

            responseObserver?.onNext(
                ConsultaKeyPorClienteResponse.newBuilder()
                    .addAllKeys(keys)
                    .build()
            )
            responseObserver?.onCompleted()
        } catch (e: ConstraintViolationException) {
            val errors = mapeiaErrors(e)

            val error = com.google.rpc.Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT.number)
                .setMessage("Erros no formulario")
                .addDetails(
                    Any.pack(
                        BadRequest.newBuilder()
                            .addAllFieldViolations(errors)
                            .build()
                    )
                )
                .build()

            responseObserver?.onError(StatusProto.toStatusRuntimeException(error))
        }

    }

    private fun mapeiaErrors(e: ConstraintViolationException): List<BadRequest.FieldViolation> {
        val errors = e.constraintViolations.map {
            BadRequest.FieldViolation.newBuilder()
                .setField(it.message)
                .build()
        }
        return errors
    }
}