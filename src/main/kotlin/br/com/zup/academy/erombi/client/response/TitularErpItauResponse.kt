package br.com.zup.academy.erombi.client.response

import io.micronaut.core.annotation.Introspected

@Introspected
data class TitularErpItauResponse(
    val id: String,
    val nome: String,
    val cpf: String
)
