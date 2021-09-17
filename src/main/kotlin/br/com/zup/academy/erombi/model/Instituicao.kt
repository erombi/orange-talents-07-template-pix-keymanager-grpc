package br.com.zup.academy.erombi.model

import javax.persistence.Embeddable

@Embeddable
class Instituicao(
    val nomeInstituicao: String,
    val ispb: String
) {

}
