package ru.popkov.coursework.repo

import com.mongodb.client.MongoDatabase
import common.Item
import org.litote.kmongo.*
import java.util.*

class MongoRepo<E> : Repo<E> {

    private val client = KMongo.createClient("mongodb://root:example@127.0.0.1:27017")
    private val mongoDatabase: MongoDatabase = client.getDatabase("admin")
    private val collection = mongoDatabase.getCollection<Map<String, E>>().apply { drop() }

    override fun create(element: E): Boolean {
        Item(UUID.randomUUID().toString(), element)
            .let {
                collection.insertOne(mapOf(it.id to element))
            }

        return true
    }

    override fun read(): List<Item<E>> {
        return collection.find().map { element ->
            Item(element.keys.first(), element.values.first())
        }.toList()
    }

    override fun read(id: String): Item<E>? {
        return collection.find(Item<E>::id eq id).map { element ->
            Item(element.keys.first(), element.values.first())
        }.firstOrNull()
    }

    override fun read(ids: List<String>): List<Item<E>> =
        ids.mapNotNull { id ->
            collection.find(Item<E>::id eq id).map { element ->
                Item(element.keys.first(), element.values.first())
            }.firstOrNull()
        }

    override fun update(id: String, value: E): Boolean {
        val query = collection.find(Item<E>::id eq id).firstOrNull()
        collection.updateOne(
            and(Item<E>::id eq id),
            setValue(Item<E>::elem, value)
        )
        return query != null
    }

    override fun delete(id: String): Boolean {
        val query = collection.deleteOne(Item<E>::id eq id)
        return query.deletedCount > 0
    }
}