package br.com.zup.academy.erombi.client

import br.com.zup.academy.erombi.client.response.ClienteErpItauResponse
import br.com.zup.academy.erombi.client.response.ContaClienteErpItauResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:9091/api/v1")
interface ErpItauClient {

    @Get("/clientes/{clienteId}")
    fun pesquisaCliente(@PathVariable clienteId: String) : ClienteErpItauResponse

    @Get("/clientes/{clienteId}/contas?tipo={tipoConta}")
    fun pesquisaContasPorCliente(@QueryValue tipoConta: String, @PathVariable clienteId: String) : ContaClienteErpItauResponse?

}