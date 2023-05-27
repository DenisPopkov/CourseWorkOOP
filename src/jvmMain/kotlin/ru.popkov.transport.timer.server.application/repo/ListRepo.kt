package ru.popkov.transport.timer.server.application.repo

import com.mongodb.client.MongoCollection
import common.Item
import org.litote.kmongo.*
import java.util.*

//class MongoRepo<E> : Repo<E> {
//    private val list = ConcurrentHashMap<String, E>()
//
//    @Suppress("KotlinConstantConditions")
//    override fun create(element: E): Boolean =
//        Item(element, UUID.randomUUID().toString())
//            .let {
//                list[it.id] = it.elem
//                true
//            }
//
//    override fun read(): List<Item<E>> =
//        list.map {
//            Item(it.value, it.key)
//        }
//
//    override fun read(id: String): Item<E>? =
//        list[id]?.let {
//            Item(it, id)
//        }
//
//    override fun read(ids: List<String>): List<Item<E>> =
//        ids.mapNotNull { id ->
//            list[id]?.let {
//                Item(it, id)
//            }
//        }
//
//    override fun update(id: String, value: E): Boolean =
//        if (list.containsKey(id)) {
//            list[id] = value
//            true
//        } else false
//
//    override fun delete(id: String): Boolean =
//        list.remove(id) != null
//
//}

//    override fun create(element: E): Boolean =
//        Item(element, UUID.randomUUID().toString())
//            .let {
//                list[it.id] = it.elem
//                true
//            }

class MongoRepo<E>(private val collection: MongoCollection<Map<String, E>>) : Repo<E> {

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