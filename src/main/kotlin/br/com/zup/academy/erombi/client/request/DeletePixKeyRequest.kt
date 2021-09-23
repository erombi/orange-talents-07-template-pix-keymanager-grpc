package br.com.zup.academy.erombi.client.request

import io.micronaut.core.annotation.Introspected

@Introspected
data class DeletePixKeyRequest(
    val key: String,
    val participant: Int
)
