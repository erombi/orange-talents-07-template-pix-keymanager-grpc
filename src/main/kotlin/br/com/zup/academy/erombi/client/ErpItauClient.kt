package br.com.zup.academy.erombi.client

import br.com.zup.academy.erombi.client.response.ClienteErpItauResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client
import java.util.*

@Client("http://localhost:9091/api/v1")
interface ErpItauClient {

    @Get("/clientes/{clienteId}")
    fun pesquisaCliente(@PathVariable clienteId: String) : ClienteErpItauResponse

}