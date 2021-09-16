package br.com.zup.academy.erombi.model.business

import java.util.regex.Pattern

class CpfValidador : Validador {
    override fun valida(key: String): Boolean {
        return Pattern.compile("^[0-9]{11}\$").matcher(key).matches()
    }
}