package br.com.zup.academy.erombi.client.response

import io.micronaut.core.annotation.Introspected

@Introspected
data class InstituicaoErpItauResponse(
    val nome: String,
    val ispb: String
)
