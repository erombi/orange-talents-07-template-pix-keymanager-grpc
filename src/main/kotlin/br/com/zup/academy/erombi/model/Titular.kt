package br.com.zup.academy.erombi.model

import java.util.*
import javax.persistence.Embeddable

@Embeddable
class Titular(
    val uuidCliente : UUID,
    val nomeCliente: String
) {

}
