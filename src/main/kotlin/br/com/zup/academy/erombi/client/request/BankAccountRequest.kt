package br.com.zup.academy.erombi.client.request

import br.com.zup.academy.erombi.TipoConta
import io.micronaut.core.annotation.Introspected

@Introspected
data class BankAccountRequest(
    val participant: Int,
    val branch: String,
    val accountNumber: Int,
    val accountType: String
)
