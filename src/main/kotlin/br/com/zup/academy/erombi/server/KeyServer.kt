package br.com.zup.academy.erombi.server

import br.com.zup.academy.erombi.*
import br.com.zup.academy.erombi.client.ErpItauClient
import br.com.zup.academy.erombi.model.Key
import br.com.zup.academy.erombi.repository.KeyRepository
import br.com.zup.academy.erombi.service.KeyService
import br.com.zup.academy.erombi.service.form.NovaKeyForm
import br.com.zup.academy.erombi.service.form.RemoveKeyForm
import com.google.rpc.ErrorDetailsProto
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import io.micronaut.validation.validator.Validator
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.util.regex.Pattern
import javax.validation.ConstraintViolationException

@Validated
@Singleton
class KeyServer(
    val service: KeyService
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
                        tipoConta
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
                                                .withDescription(e.constraintViolations.first().message)
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
        }
    }
}