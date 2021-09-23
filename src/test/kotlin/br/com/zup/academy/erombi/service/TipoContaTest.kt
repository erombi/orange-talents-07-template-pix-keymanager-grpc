package br.com.zup.academy.erombi.service

import br.com.zup.academy.erombi.TipoConta
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TipoContaTest{

    @Test
    fun `deve retornar UNKNOWN_TIPO_CONTA`() {
        val porExtenso = TipoConta.UNKNOWN_TIPO_CONTA.toPorExtenso()

        assertEquals("UNKNOWN_TIPO_CONTA", porExtenso)
    }

    @Test
    fun `deve retornar CONTA_CORRENTE`() {
        val porExtenso = TipoConta.CACC.toPorExtenso()

        assertEquals("CONTA_CORRENTE", porExtenso)
    }

    @Test
    fun `deve retornar CONTA_POUPANCA`() {
        val porExtenso = TipoConta.SVGS.toPorExtenso()

        assertEquals("CONTA_POUPANCA", porExtenso)
    }

}