package br.com.zup.academy.erombi.model.business

import java.util.regex.Pattern

class PhoneValidador : Validador {
    override fun valida(key: String): Boolean {
        return Pattern.compile("^\\+[1-9][0-9]\\d{1,14}\$").matcher(key).matches()
    }
}