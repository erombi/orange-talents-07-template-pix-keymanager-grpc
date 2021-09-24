package br.com.zup.academy.erombi.client.response

import br.com.zup.academy.erombi.TipoKey
import br.com.zup.academy.erombi.client.request.BankAccountRequest
import br.com.zup.academy.erombi.client.request.OwnerRequest
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime

@Introspected
data class PixKeyDetailsResponse(
    val keyType: TipoKey,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest,
    val createdAt: LocalDateTime
) {
}
