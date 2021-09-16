package br.com.zup.academy.erombi.client.response

import io.micronaut.core.annotation.Introspected
import java.util.*

@Introspected
data class ClienteErpItauResponse(
    val id : UUID,
    val nome: String,
    val cpf: String,
    val instituicao : InstituicaoErpItauResponse
)
