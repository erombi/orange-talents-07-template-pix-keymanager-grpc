package br.com.zup.academy.erombi.model.annotation

import br.com.zup.academy.erombi.TipoConta
import br.com.zup.academy.erombi.client.ErpItauClient
import br.com.zup.academy.erombi.client.response.ClienteErpItauResponse
import br.com.zup.academy.erombi.client.response.InstituicaoErpItauResponse
import br.com.zup.academy.erombi.service.form.NovaKeyForm
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*

@MicronautTest(transactional = false)
internal class ValidaKeyValidatorTest(
    val validator: Validator
) {

    @Inject
    lateinit var clientErpItau: ErpItauClient

    val idCLiente = UUID.randomUUID().toString()

    @Test
    fun `deve retornar True quando Key for nulo`() {
        Mockito.`when`(
            clientErpItau.pesquisaCliente(idCLiente)
        ).thenReturn(
            ClienteErpItauResponse(
                UUID.fromString(idCLiente),
                "Leonardo Silva",
                "40764442058",
                InstituicaoErpItauResponse(
                    "ITAÚ UNIBANCO S.A.",
                    "60701190"
                )
            )
        )

        val validate = validator.validate(
            NovaKeyForm(
                idCLiente,
                null,
                "",
                TipoConta.CACC
            )
        )

        assertTrue(validate.isEmpty())
    }

    @MockBean(ErpItauClient::class)
    fun erpItau(): ErpItauClient? {
        return Mockito.mock(ErpItauClient::class.java)
    }

}