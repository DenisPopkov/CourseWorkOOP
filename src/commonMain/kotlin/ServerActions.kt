package com.jonastm.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.*
import kotlinx.serialization.encoding.*

@Serializable
sealed class ServerAction {
    val time: Instant = Clock.System.now()
}

@Serializable
@SerialName("user_msg")
class UserMessageAction(
    val id: Int,
    val tripNumber: String,
    val tripComment: String,
) : ServerAction()

@Serializable
@SerialName("remove")
class RemoveAction(
    val id: Int
) : ServerAction()

interface ServerActionHandler {
    suspend fun onUserMessage(action: UserMessageAction)
    suspend fun onRemoveMessage(action: RemoveAction)
    suspend fun onError(e: Exception)
}

suspend fun onAction(action: ServerAction, handler: ServerActionHandler) {
    when (action) {
        is UserMessageAction -> handler.onUserMessage(action)
        is RemoveAction -> handler.onRemoveMessage(action)
    }
}
