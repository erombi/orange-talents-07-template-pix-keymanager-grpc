package br.com.zup.academy.erombi.model.annotation

import br.com.zup.academy.erombi.TipoConta
import br.com.zup.academy.erombi.service.form.NovaKeyForm
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest(transactional = false)
internal class ValidaKeyValidatorTest(
    val validator: Validator
) {

    @Test
    fun `deve retornar True quando Key for nulo`() {
        val validate = validator.validate(
            NovaKeyForm(
                UUID.randomUUID().toString(),
                null,
                "",
                TipoConta.CONTA_CORRENTE
            )
        )

        assertTrue(validate.isEmpty())
    }

}