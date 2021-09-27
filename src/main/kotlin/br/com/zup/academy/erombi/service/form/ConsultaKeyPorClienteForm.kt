package br.com.zup.academy.erombi.service.form

import br.com.zup.academy.erombi.model.annotation.ValidaCliente
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class ConsultaKeyPorClienteForm(
    @field:NotBlank
    @field:ValidaCliente
    val idCliente: String
)
