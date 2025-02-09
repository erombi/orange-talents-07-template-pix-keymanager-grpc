package br.com.zup.academy.erombi.repository

import br.com.zup.academy.erombi.model.Key
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface KeyRepository : JpaRepository<Key, UUID> {

    fun findByIdAndTitularUuidCliente(id: UUID, uuidCliente: String): Optional<Key>
    fun findByKey(key: String): Optional<Key>
    fun findAllByTitularUuidCliente(uuidCliente: String): Set<Key>

    fun existsByKey(key: String): Boolean
    fun existsByIdAndTitularUuidCliente(id: UUID, uuidCliente: String): Boolean

    fun deleteByIdAndTitularUuidCliente(id: UUID, uuidCliente: String)
}