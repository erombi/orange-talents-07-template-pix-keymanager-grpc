package br.com.zup.academy.erombi.model

import br.com.zup.academy.erombi.TipoConta
import br.com.zup.academy.erombi.TipoKey
import br.com.zup.academy.erombi.model.annotation.ValidaKey
import java.util.*
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class Key(
    tipoKey: TipoKey,
    key: String,
    tipoConta: TipoConta,
    instituicao : Instituicao,
    agencia : String,
    numero: String,
    titular: Titular,
) {

    @Id
    var id : UUID? = null

    @NotNull
    val tipoKey = tipoKey

    @NotBlank
    @Column(length = 77)
    var key = key

    @NotNull
    val tipoConta = tipoConta

    @Embedded
    val instituicao = instituicao

    val agencia = agencia

    val numero = numero

    @Embedded
    val titular = titular

    init {
        id = UUID.randomUUID()
    }

}