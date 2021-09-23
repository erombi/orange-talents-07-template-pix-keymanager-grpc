package br.com.zup.academy.erombi.model

import javax.persistence.Embeddable

@Embeddable
class Instituicao(
    val participant: Int,
    val nomeInstituicao: String,
    val ispb: String
) {

}
