package br.com.zup.academy.erombi.model.business

import java.util.regex.Pattern

class CnpjValidador : Validador {
    override fun valida(key: String): Boolean {
        return Pattern.compile("^\\d{2}\\.\\d{3}\\.\\d{3}\\/\\d{4}\\-\\d{2}\$").matcher(key).matches()
    }
}