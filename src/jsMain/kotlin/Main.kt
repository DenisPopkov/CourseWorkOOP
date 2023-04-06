import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.protobuf.ProtoBuf
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

var currentMessage = 1
val firebaseApp by lazy {
    Firebase.initialize(
        options = FirebaseOptions(
            applicationId = "1:707606191670:android:7c1346caf0dc5ef6604297",
            apiKey = "",
            storageBucket = "",
            projectId = "transporttimerbd",
        )
    )
}

private suspend fun <T> addToBD(userName: String, message: T) {
    val firestore = Firebase.firestore(firebaseApp)
    val newData = hashMapOf(
        "${currentMessage}_$userName" to message,
    )
    firestore
        .collection("TransportTimer")
        .document("kietJiwINlvw6srvCSCE")
        .update(newData)

    currentMessage++
}

private suspend fun readFromBD(userName: String): Any {
    val firestore = Firebase.firestore(firebaseApp)

    return firestore
        .collection("TransportTimer")
        .document("kietJiwINlvw6srvCSCE")
        .parent
        .get()
        .documents.last()
}

private val USER_NAME = listOf(
    "Denis",
    "Artem",
    "Angelina",
    "Vlad"
).random()

fun main() {

    val transportTrips = mutableStateListOf<UserMessageAction>()
    val handler = ServerActionHandlerImpl(transportTrips)
    val chat = ChatStream(handler)
    var inputText = ""

    renderComposable(rootElementId = "root") {
        Div({ style { padding(25.px) } }) {

            H1 { Text("Transport Room") }

            Hr()

            Div(attrs = {
                style {
                    height(300.px)
                    overflow("auto")
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.ColumnReverse)
                }
            }) {
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                    }
                }) {
                    transportTrips.forEach {
                        val message = it
                        Div(attrs = {
                            style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Row)
                                alignItems(AlignItems.Baseline)
                                gap(10.px)
                            }
                        }) {
                            val msg = "[${it.tripNumber}]: ${it.tripComment}"
                            P {
                                Text(convertTime(message.time))
                            }
                            P(attrs = {
                                style {
                                    fontWeight(500)
                                }
                            }) {
                                Text(msg)
                            }
                            Button(attrs = {
                                onClick {
                                    CoroutineScope(Dispatchers.Default).launch {
                                        chat.send(DeleteMessageAction(message.id)) { exception ->
                                            transportTrips.add(UserMessageAction(0, "${exception.message}", "ERROR"))
                                        }
                                    }
                                }
                            }) {
                                Text("X")
                            }
                        }
                    }
                }
            }

            Hr()

            TextArea(attrs = {
                style {
                    width(300.px)
                    height(50.px)
                }
                onInput {
                    inputText = it.value.trim()
                }
            })

            Button(attrs = {
                onClick {
                    CoroutineScope(Dispatchers.Default).launch {
                        if (inputText.isNotBlank()) {
                            addToBD(USER_NAME, inputText)
                            readFromBD(USER_NAME)
                            chat.send(NewMessageAction(inputText)) {
                                transportTrips.add(UserMessageAction(1, "${it.message}", "ERROR"))
                            }
                            inputText = ""
                        }
                    }
                }
            }) {
                Text("Send Message")
            }
        }
    }
}

class ServerActionHandlerImpl(
    private val transportTrips: SnapshotStateList<UserMessageAction>
) : ServerActionHandler {
    override suspend fun onError(e: Exception) {
        transportTrips.add(
            UserMessageAction(0, tripNumber = "${e.message}", tripComment = "ERROR")
        )
    }

    override suspend fun onRemoveMessage(action: RemoveAction) {
        var itemToRemove: UserMessageAction? = null
        for (message in transportTrips) {
            if (message.id == action.id) {
                itemToRemove = message
                break
            }
        }
        transportTrips.remove(itemToRemove)
    }

    override suspend fun onUserMessage(action: UserMessageAction) {
        transportTrips.add(action)
    }
}

class ChatStream(
    private val serverActionHandler: ServerActionHandler
) {

    private val client = HttpClient(Js) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(ProtoBuf)
        }
    }

    private var wsSession: DefaultClientWebSocketSession? = null

    init {
        CoroutineScope(Dispatchers.Default).launch {
            client.webSocket(method = HttpMethod.Get, path = "/ws") {
                wsSession = this
                receive<ServerAction> {
                    onAction(it, serverActionHandler)
                }
            }
        }
    }

    suspend fun send(msg: ClientAction, onError: (Exception) -> Unit) {
        wsSession?.sendMessage(msg, onError) ?: apply {
            onError(Exception("No connected to chat"))
        }
    }
}

fun convertTime(instant: Instant): String {
    val time = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = when {
        (time.hour > 9) -> "${time.hour}"
        else -> " ${time.hour}"
    }
    val minutes = when {
        (time.minute > 9) -> "${time.minute}"
        else -> " ${time.minute}"
    }
    return "$hour:$minutes"
}

suspend inline fun <reified T> DefaultClientWebSocketSession.receive(onMessage: (T) -> Unit) {
    try {
        while (true) {
            val msg = receiveDeserialized<T>()
            onMessage(msg)
        }
    } catch (e: Exception) {
        println("Error while receiving: " + e.message)
    }
}

suspend inline fun <reified T> DefaultClientWebSocketSession.sendMessage(msg: T, onError: (Exception) -> Unit) {
    try {
        sendSerialized(msg)
    } catch (e: Exception) {
        println("Error while sending: " + e.message)
        onError(e)
        return
    }
}
