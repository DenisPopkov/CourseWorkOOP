package ru.popkov.transport.timer.server.application

import ClientAction
import ClientActionHandler
import DeleteMessageAction
import NewMessageAction
import RemoveAction
import ServerAction
import UserMessageAction
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.html.*
import kotlinx.serialization.protobuf.ProtoBuf
import onAction
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

private val connections: MutableSet<Connection> = Collections.synchronizedSet(LinkedHashSet())

suspend fun sendAll(action: ServerAction) {
    connections.forEach { conn ->
        conn.send(action)
    }
}

class ClientActionHandlerImpl(private val connection: Connection) : ClientActionHandler {
    override suspend fun onNewMessage(action: NewMessageAction) {
        sendAll(
            UserMessageAction(
                Random.nextLong().toInt(),
                action.text,
                connection.name
            )
        )
    }

    override suspend fun onDeleteMessage(action: DeleteMessageAction) {
        sendAll(RemoveAction(action.id))
    }

    override suspend fun onError(e: Exception) {
        e.printStackTrace()
    }
}

class Connection(
    private val session: WebSocketServerSession
) {

    companion object {
        val lastId = AtomicInteger(0)
    }

    val name = "user${lastId.getAndIncrement()}"

    suspend fun send(msg: ServerAction) {
        session.sendSerialized(msg)
    }
}

fun main() {
    embeddedServer(Netty, port = 8087, host = "127.0.0.1", module = Application::myApplicationModule).start(wait = true)
}

fun Application.configuration() {
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeaders { true }
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(ProtoBuf)
    }
}

fun Application.routes() {
    routing {
        get("/") {
            call.respondHtml(HttpStatusCode.OK, HTML::index)
        }
        static("/static") {
            resources()
        }
        webSocket("/ws") {
            handleNewConnection()
        }
    }
}

fun HTML.index() {
    head {
        title("TransportTimerServer")
    }
    body {
        div {
            id = "root"
        }
        script(src = "/static/TransportTimerServer.js") {}
    }
}

suspend fun WebSocketServerSession.handleNewConnection() {
    val thisConnection = Connection(this)
    connections.add(thisConnection)
    println("Added ${thisConnection.name}")

    val handler = ClientActionHandlerImpl(thisConnection)

    try {
        thisConnection.send(
            UserMessageAction(
                Random.nextLong().toInt(),
                tripNumber = "Welcome to our server",
                tripComment = "Server"
            )
        )
        while (true) {
            val action = receiveDeserialized<ClientAction>()
            onAction(action, handler)
        }
    } catch (e: Exception) {
        println(e.localizedMessage)
    } finally {
        println("Removing $thisConnection!")
        connections.remove(thisConnection)
    }
}

fun Application.myApplicationModule() {
    configuration()
    routes()
}
