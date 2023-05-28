package ru.popkov.coursework.repo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.FindOneAndUpdateOptions
import common.Item
import io.kotest.assertions.print.print
import org.litote.kmongo.*
import java.util.*

class MongoRepo<E>(private val collection: MongoCollection<E>) : Repo<E> {

    private val ids = mutableListOf<String>()

    override fun create(element: E): Boolean {
        Item(UUID.randomUUID().toString(), element)
            .let {
                ids.add(it.id)
                collection.insertOne(element)
            }

        return true
    }

    override fun read(): List<Item<E>> {
        return collection.find().mapIndexed { index, element ->
            Item(ids[index], element)
        }.toList()
    }

    override fun read(id: String): Item<E>? {
        return collection.find().mapIndexed { index, element ->
            Item(ids[index], element)
        }.firstOrNull()
    }

    override fun read(ids: List<String>): List<Item<E>> =
        ids.mapNotNull { id ->
            collection.find(Item<E>::id eq id).mapIndexed { index, element ->
                Item(ids[index], element)
            }.firstOrNull()
        }

    override fun update(id: String, value: E): Boolean {
        collection.updateOneById(id, value as Any)
        return true
    }

    override fun delete(id: String): Boolean {
        val query = collection.deleteOneById(id)
        return query.deletedCount > 0
    }
}