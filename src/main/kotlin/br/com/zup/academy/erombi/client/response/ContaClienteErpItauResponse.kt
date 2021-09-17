package br.com.zup.academy.erombi.client.response

import io.micronaut.core.annotation.Introspected

@Introspected
data class ContaClienteErpItauResponse(
    val tipo: String,
    val instituicao: InstituicaoErpItauResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularErpItauResponse,
)
