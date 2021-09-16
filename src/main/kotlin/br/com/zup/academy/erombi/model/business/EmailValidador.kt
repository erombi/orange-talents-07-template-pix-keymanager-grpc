package br.com.zup.academy.erombi.model.business

import java.util.regex.Pattern

class EmailValidador : Validador {
    override fun valida(key: String): Boolean {
        return Pattern.compile("[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?").matcher(key).matches()
    }
}