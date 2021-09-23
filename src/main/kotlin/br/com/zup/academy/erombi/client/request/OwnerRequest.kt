package br.com.zup.academy.erombi.client.request

import io.micronaut.core.annotation.Introspected

@Introspected
data class OwnerRequest(
    val type: String,
    val name: String,
    val taxIdNumber: String
    )
