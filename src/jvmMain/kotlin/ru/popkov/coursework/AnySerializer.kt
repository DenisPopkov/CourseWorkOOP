package ru.popkov.coursework

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer

inline fun <reified T : Any> anySerializer(): AnySerializer<T> {
    return AnySerializer(Json.serializersModule.serializer())
}

class AnySerializer<T : Any>(private val dataSerializer: KSerializer<T>) : KSerializer<T> {
    override val descriptor: SerialDescriptor = dataSerializer.descriptor

    override fun serialize(encoder: Encoder, value: T) {
        val jsonEncoder =
            encoder as? JsonEncoder ?: throw SerializationException("This serializer can only be used with JSON")
        jsonEncoder.encodeJsonElement(Json.encodeToJsonElement(dataSerializer, value))
    }

    override fun deserialize(decoder: Decoder): T {
        val jsonDecoder =
            decoder as? JsonDecoder ?: throw SerializationException("This serializer can only be used with JSON")
        return jsonDecoder.decodeJsonElement().let {
            Json.decodeFromJsonElement(dataSerializer, it)
        }
    }
}

private fun Any?.toJsonPrimitive(): JsonPrimitive {
    return when (this) {
        null -> JsonNull
        is JsonPrimitive -> this
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        else -> throw Exception("${this::class}")
    }
}

private fun JsonPrimitive.toAnyValue(): Any? {
    val content = this.content
    return when {
        this.isString -> content
        content.equals("null", ignoreCase = true) -> null
        content.equals("true", ignoreCase = true) -> true
        content.equals("false", ignoreCase = true) -> false
        content.toIntOrNull() != null -> content.toInt()
        content.toLongOrNull() != null -> content.toLong()
        content.toDoubleOrNull() != null -> content.toDouble()
        else -> throw Exception("content：$content")
    }
}

private fun Any?.toJsonElement(): JsonElement {
    return when (this) {
        null -> JsonNull
        is JsonElement -> this
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        is Iterable<*> -> JsonArray(this.map { it.toJsonElement() })
        is Map<*, *> -> JsonObject(this.map { it.key.toString() to it.value.toJsonElement() }.toMap())
        else -> throw Exception("${this::class}=${this}}")
    }
}

private fun JsonElement.toAnyOrNull(): Any? {
    return when (this) {
        is JsonNull -> null
        is JsonPrimitive -> toAnyValue()
        is JsonObject -> this.map { it.key to it.value.toAnyOrNull() }.toMap()
        is JsonArray -> this.map { it.toAnyOrNull() }
    }
}