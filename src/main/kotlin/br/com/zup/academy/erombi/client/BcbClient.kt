package br.com.zup.academy.erombi.client

import br.com.zup.academy.erombi.client.request.CreatePixKeyRequest
import br.com.zup.academy.erombi.client.request.DeletePixKeyRequest
import br.com.zup.academy.erombi.client.response.CreatePixKeyResponse
import io.micronaut.context.annotation.Primary
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import org.hibernate.annotations.FetchProfile

@Client("http://localhost:8082/api/v1/pix")
interface BcbClient {

    @Post("/keys", produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun cadastraNoBancoCentral(@Body request: CreatePixKeyRequest) : CreatePixKeyResponse

    @Delete("/keys/{key}", produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun deletaDoBancoCentral(@PathVariable key: String, @Body request: DeletePixKeyRequest)
}