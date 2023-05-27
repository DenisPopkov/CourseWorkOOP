package ru.popkov.transport.timer.server.application.rest

import common.Item
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import ru.popkov.transport.timer.server.application.AnySerializer
import ru.popkov.transport.timer.server.application.anySerializer
import ru.popkov.transport.timer.server.application.repo.Repo

inline fun <reified T : Any> Route.repoRoutes(
    repo: Repo<T>
) {
    val serializer: AnySerializer<T> = anySerializer()
    val itemSerializer: AnySerializer<Item<T>> = anySerializer()
    val listItemSerializer = ListSerializer(itemSerializer)

    get {
        val elemList: List<Item<T>> = repo.read()
        if (elemList.isEmpty()) {
            call.respondText("No element found", status = HttpStatusCode.NotFound)
        } else {
            val elemJson = Json.encodeToString(listItemSerializer, elemList)
            call.respond(elemJson)
        }
    }
    get("{id}") {
        val id =
            call.parameters["id"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
        val item =
            repo.read(id) ?: return@get call.respondText(
                "No element with id $id",
                status = HttpStatusCode.NotFound
            )
        val itemJson = Json.encodeToString(itemSerializer, item)
        call.respond(itemJson)
    }
    post("byId") {
        val ids = try {
            call.receive<List<String>>()
        } catch (e: Throwable) {
            return@post call.respondText(
                "Request body is not list id", status = HttpStatusCode.BadRequest
            )
        }
        val elements = Json.encodeToString(listItemSerializer, repo.read(ids))
        call.respond(elements)
    }
    post {
        val elemJson = call.receive<String>()
        val elem = Json.decodeFromString(serializer, elemJson)
        repo.create(elem)
        call.respondText(
            "Element stored correctly",
            status = HttpStatusCode.Created
        )
    }
    delete("{id}") {
        val id = call.parameters["id"]
            ?: return@delete call.respond(HttpStatusCode.BadRequest)
        if (repo.delete(id)) {
            call.respondText(
                "Element removed correctly",
                status = HttpStatusCode.Accepted
            )
        } else {
            call.respondText(
                "No element with id $id",
                status = HttpStatusCode.NotFound
            )
        }
    }
    put("{id}") {
        val id = call.parameters["id"] ?: return@put call.respondText(
            "Missing or malformed id",
            status = HttpStatusCode.BadRequest
        )
        repo.read(id) ?: return@put call.respondText(
            "No element with id $id",
            status = HttpStatusCode.NotFound
        )
        val newElementJson = call.receive<String>()
        val newElement = Json.decodeFromString(serializer, newElementJson)
        repo.update(id, newElement)
        call.respondText(
            "Element updates correctly",
            status = HttpStatusCode.Created
        )
    }
}