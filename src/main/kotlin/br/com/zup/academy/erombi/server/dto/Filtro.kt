package br.com.zup.academy.erombi.server.dto

import br.com.zup.academy.erombi.ConsultaKeyResponse
import br.com.zup.academy.erombi.client.BcbClient
import br.com.zup.academy.erombi.exception.NotFoundException
import br.com.zup.academy.erombi.repository.KeyRepository
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtra(repository: KeyRepository, bcbClient: BcbClient): ConsultaKeyResponse

    @Introspected
    data class PorPix(
        @field:NotBlank val clienteId: String,
        @field:NotBlank val pixId: String
    ) : Filtro() {

        private fun pixIdAsUuid() = UUID.fromString(pixId)

        override fun filtra(repository: KeyRepository, bcbClient: BcbClient): ConsultaKeyResponse {
            return ConsultaKeyConverter().converter(
                repository.findByIdAndTitularUuidCliente(pixIdAsUuid(), clienteId).orElseThrow { NotFoundException("Pix não encontrado !") }
            )
        }
    }

    @Introspected
    data class PorChave(
        @field:NotBlank @field:Size(max = 77) val key : String
    ) : Filtro() {

        private val logger = LoggerFactory.getLogger(PorChave::class.java)

        override fun filtra(repository: KeyRepository, bcbClient: BcbClient): ConsultaKeyResponse {
            val possivelKey = repository.findByKey(key)

            if (possivelKey.isPresent) {
                return ConsultaKeyConverter().converter(possivelKey.get())
            }

            logger.info("Chave não encontrada, consultando Banco central")
            val response = bcbClient.consultaKey(key)

            return when (response.status) {
                HttpStatus.OK -> ConsultaKeyConverter().converter(response.body()!!)

                else -> throw NotFoundException("Key não encontrada !")
            }
        }
    }

    @Introspected
    class Invalido : Filtro() {
        override fun filtra(repository: KeyRepository, bcbClient: BcbClient): ConsultaKeyResponse {
            throw IllegalStateException("Filtro inexistente !")
        }
    }

}
