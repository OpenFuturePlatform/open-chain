package io.openfuture.chain.entity.base

import java.io.Serializable
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.*
import javax.persistence.Id
import javax.persistence.MappedSuperclass

/**
 * @author Homza Pavel
 */
@MappedSuperclass
abstract class BaseModel(@Id @GeneratedValue(strategy = IDENTITY) var id: Int = 0) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseModel

        if (id == 0) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}
