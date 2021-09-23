package br.com.zup.academy.erombi.service.form

import br.com.zup.academy.erombi.NovaKeyRequest
import br.com.zup.academy.erombi.TipoConta
import br.com.zup.academy.erombi.model.annotation.UniqueKey
import br.com.zup.academy.erombi.model.annotation.ValidaCliente
import br.com.zup.academy.erombi.model.annotation.ValidaKey
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidaKey
@Introspected
data class NovaKeyForm(

    @field:ValidaCliente
    @field:NotBlank
    val uuidCliente: String?,

    val tipoKey: NovaKeyRequest.TipoKey?,

    @field:Size(max = 77)
    @field:UniqueKey
    @field:NotNull
    val key: String?,

    @field:NotNull
    val tipoConta: TipoConta?,
)
