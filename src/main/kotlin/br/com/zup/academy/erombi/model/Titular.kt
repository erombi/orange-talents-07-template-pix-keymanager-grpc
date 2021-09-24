package br.com.zup.academy.erombi.model

import javax.persistence.Embeddable

@Embeddable
class Titular(
    val uuidCliente : String,
    val nomeCliente: String,
    val cpfCliente: String
) {

}
