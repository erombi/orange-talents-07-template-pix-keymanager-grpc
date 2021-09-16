package br.com.zup.academy.erombi.repository

import br.com.zup.academy.erombi.model.Key
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface KeyRepository : JpaRepository<Key, UUID> {
}