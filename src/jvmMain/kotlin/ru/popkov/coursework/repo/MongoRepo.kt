package ru.popkov.coursework.repo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import common.Item
import io.ktor.server.html.*
import org.bson.Document
import org.litote.kmongo.*
import java.util.*

class MongoRepo<E>(private val collection: MongoCollection<E>) : Repo<E> {

    override fun create(element: E): Boolean {
        collection.insertOne(element)
        return true
    }

    override fun read(): List<Item<E>> {
        val idsDocuments = collection.withDocumentClass<Document>().find().distinct()
        return collection.find().mapIndexed { index, element ->
            val itemId = idsDocuments[index].values.elementAt(0).toString()
            Item(itemId, element)
        }.toList()
    }

    override fun read(id: String): Item<E>? {
        return collection.find().map { element ->
            Item(id, element)
        }.firstOrNull()
    }

    override fun read(ids: List<String>): List<Item<E>> =
        ids.mapNotNull { id ->
            collection.find(Item<E>::id eq id).mapIndexed { index, element ->
                Item(ids[index], element)
            }.firstOrNull()
        }

    override fun update(id: String, value: E): Boolean {
        collection.updateOne(Filters.ne("id", id), value as Any)
        return true
    }

    override fun delete(id: String): Boolean {
        val query = collection.deleteOne(Filters.ne("id", id))
        return query.deletedCount > 0
    }
}