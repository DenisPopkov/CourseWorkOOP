package com.jonastm.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.*

@Serializable
sealed class ServerAction {
    val time: Instant = Clock.System.now()
}

object UserMessageActionSerializer : KSerializer<UserMessageAction> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("UserMessageAction") {
        element("dataType", serialDescriptor<String>())
        element("tripNumber", buildClassSerialDescriptor("Any"))
        element("tripComment", buildClassSerialDescriptor("Any"))
    }

    @Suppress("UNCHECKED_CAST")
    private val dataTypeSerializers: Map<String, KSerializer<Any>> =
        mapOf(
            "String" to serializer<String>(),
            "Int" to serializer<Int>(),
        ).mapValues { (_, v) -> v as KSerializer<Any> }

    private fun getPayloadSerializer(dataType: String): KSerializer<Any> = dataTypeSerializers[dataType]
        ?: throw SerializationException("Serializer for class $dataType is not registered in UserMessageActionSerializer")

    override fun serialize(encoder: Encoder, value: UserMessageAction) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.dataType)
            encodeSerializableElement(descriptor, 1, getPayloadSerializer(value.dataType), value.tripNumber)
            encodeSerializableElement(descriptor, 2, getPayloadSerializer(value.dataType), value.tripComment)
        }
    }

    @ExperimentalSerializationApi
    override fun deserialize(decoder: Decoder): UserMessageAction = decoder.decodeStructure(descriptor) {
        if (decodeSequentially()) {
            val dataType = decodeStringElement(descriptor, 0)
            val tripNumber = decodeSerializableElement(descriptor, 1, getPayloadSerializer(dataType))
            val tripComment = decodeSerializableElement(descriptor, 2, getPayloadSerializer(dataType))
            UserMessageAction("0", dataType, tripNumber, tripComment)
        } else {
            require(decodeElementIndex(descriptor) == 0) { "dataType field should precede payload field" }
            val dataType = decodeStringElement(descriptor, 0)
            val tripNumber = when (val index = decodeElementIndex(descriptor)) {
                1 -> decodeSerializableElement(descriptor, 1, getPayloadSerializer(dataType))
                CompositeDecoder.DECODE_DONE -> throw SerializationException("tripNumber field is missing")
                else -> error("Unexpected index: $index")
            }
            val tripComment = when (val index = decodeElementIndex(descriptor)) {
                2 -> decodeSerializableElement(descriptor, 1, getPayloadSerializer(dataType))
                CompositeDecoder.DECODE_DONE -> throw SerializationException("tripNumber field is missing")
                else -> error("Unexpected index: $index")
            }
            UserMessageAction("0", dataType, tripNumber, tripComment)
        }
    }
}

@Serializable(with = UserMessageActionSerializer::class)
@SerialName("user_msg")
class UserMessageAction(
    val id: String,
    val dataType: String = "String",
    val tripNumber: Any,
    val tripComment: Any,
) : ServerAction()

@Serializable
@SerialName("remove")
class RemoveAction(
    val id: String
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
