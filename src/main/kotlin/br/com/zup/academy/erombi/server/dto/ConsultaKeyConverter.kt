package br.com.zup.academy.erombi.server.dto

import br.com.zup.academy.erombi.ConsultaKeyResponse
import br.com.zup.academy.erombi.TipoConta
import br.com.zup.academy.erombi.client.response.PixKeyDetailsResponse
import br.com.zup.academy.erombi.model.Key
import com.google.protobuf.Timestamp

class ConsultaKeyConverter {

    fun converter(key: Key): ConsultaKeyResponse {
        return ConsultaKeyResponse.newBuilder()
            .setClienteId(key.titular.uuidCliente.toString())
            .setPixId(key.id.toString())
            .setChave(
                ConsultaKeyResponse.ChavePix.newBuilder()
                .setTipoKey(key.tipoKey)
                .setKey(key.key)
                .setConta(
                    ConsultaKeyResponse.ChavePix.ContaInfo.newBuilder()
                    .setTipo(key.tipoConta)
                    .setInstituicao(key.instituicao.nomeInstituicao)
                    .setNomeTitular(key.titular.nomeCliente)
                    .setCpfTitular(key.titular.cpfCliente)
                    .setAgencia(key.agencia)
                    .setNumeroConta(key.numero)
                    .build())
                .setCriadaEm(Timestamp.getDefaultInstance())
                .build())
            .build()
    }

    fun converter(key: PixKeyDetailsResponse): ConsultaKeyResponse {
        return ConsultaKeyResponse.newBuilder()
            .setChave(
                ConsultaKeyResponse.ChavePix.newBuilder()
                .setTipoKey(key.keyType)
                .setKey(key.key)
                .setConta(
                    ConsultaKeyResponse.ChavePix.ContaInfo.newBuilder()
                    .setTipo(TipoConta.valueOf(key.bankAccount.accountType))
                    .setNomeTitular(key.owner.name)
                    .setCpfTitular(key.owner.taxIdNumber)
                    .setAgencia(key.bankAccount.branch)
                    .setNumeroConta(key.bankAccount.accountNumber.toString())
                    .build())
                .setCriadaEm(Timestamp.getDefaultInstance())
                .build())
            .build()
    }
}