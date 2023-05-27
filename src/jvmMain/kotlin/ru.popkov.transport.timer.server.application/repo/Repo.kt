package ru.popkov.transport.timer.server.application.repo

import common.Item

interface Repo<E> {
    fun create(element: E): Boolean
    fun read(): List<Item<E>>
    fun read(id: String): Item<E>?
    fun read(ids: List<String>): List<Item<E>>
    fun update(id: String, value: E): Boolean
    fun delete(id: String): Boolean
}