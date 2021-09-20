package br.com.zup.academy.erombi.service.form

import br.com.zup.academy.erombi.model.annotation.ValidaCliente
import br.com.zup.academy.erombi.model.annotation.ValidaClienteKey
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@ValidaClienteKey
@Introspected
data class RemoveKeyForm(

    @field:NotBlank
    val idKey: String,

    @field:NotBlank
    @field:ValidaCliente
    val idCliente: String
)
