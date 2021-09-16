package br.com.zup.academy.erombi.model.business

interface Validador {

    fun valida(key: String): Boolean;
}