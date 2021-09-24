package br.com.zup.academy.erombi

import br.com.zup.academy.erombi.client.response.PixKeyDetailsResponse
import br.com.zup.academy.erombi.server.dto.Filtro
import javax.validation.ConstraintViolationException
import javax.validation.Validator


fun ConsultaKeyRequest.toModel(validator: Validator): Filtro {

    val filtro = when(filtroCase) {
        ConsultaKeyRequest.FiltroCase.KEY -> {
            Filtro.PorChave(key)
        }
        ConsultaKeyRequest.FiltroCase.PIX -> {
            pix.let {
                Filtro.PorPix(it.idCliente, it.idKey)
            }
        }
        ConsultaKeyRequest.FiltroCase.FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty())
        throw ConstraintViolationException(violations)

    return filtro
}