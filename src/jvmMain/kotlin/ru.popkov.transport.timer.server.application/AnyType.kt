package ru.popkov.transport.timer.server.application

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class AnyData(
    val name: String,
    @Serializable(with = AnyValueSerializer::class)
    val anyValue: Any?,
    val anyList: List<@Serializable(with = AnyValueSerializer::class) Any?>,
    val anyMap: Map<@Serializable(with = AnyValueSerializer::class) Any?, @Serializable(with = AnySerializer::class) Any?>,
    @Serializable(with = DataFrameSerializer::class)
    val dataFrame: Map<String, List<@Contextual Any?>>,
)

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

object AnyValueSerializer : KSerializer<Any?> {
    private val delegateSerializer = JsonPrimitive.serializer()
    override val descriptor = delegateSerializer.descriptor
    override fun serialize(encoder: Encoder, value: Any?) {
        encoder.encodeSerializableValue(delegateSerializer, value.toJsonPrimitive())
    }

    override fun deserialize(decoder: Decoder): Any? {
        val jsonPrimitive = decoder.decodeSerializableValue(delegateSerializer)
        return jsonPrimitive.toAnyValue()
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

object AnySerializer : KSerializer<Any?> {
    private val delegateSerializer = JsonElement.serializer()
    override val descriptor = delegateSerializer.descriptor
    override fun serialize(encoder: Encoder, value: Any?) {
        encoder.encodeSerializableValue(delegateSerializer, value.toJsonElement())
    }

    override fun deserialize(decoder: Decoder): Any? {
        val jsonPrimitive = decoder.decodeSerializableValue(delegateSerializer)
        return jsonPrimitive.toAnyOrNull()
    }
}

object DataFrameSerializer : KSerializer<Map<String, List<Any?>>> {
    private val stdDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ")
    private val delegateSerializer = JsonObject.serializer()
    override val descriptor = delegateSerializer.descriptor
    override fun serialize(encoder: Encoder, value: Map<String, List<Any?>>) {
        val jsonObject = JsonObject(value.mapValues {
            when (it.key) {
                "date" -> JsonArray(it.value.map { v -> JsonPrimitive(stdDateTimeFormatter.format(v as OffsetDateTime)) })
                "price" -> JsonArray(it.value.map { v -> JsonPrimitive(v as Double) })
                else -> throw IllegalStateException("Unknown key${it.key}")
            }
        })
        encoder.encodeSerializableValue(delegateSerializer, jsonObject)
    }

    override fun deserialize(decoder: Decoder): Map<String, List<Any?>> {
        val jsonObject = decoder.decodeSerializableValue(delegateSerializer)
        val map = jsonObject.mapValues {
            when (it.key) {
                "date" -> it.value.jsonArray.map { v -> OffsetDateTime.parse(v.jsonPrimitive.content, stdDateTimeFormatter) }
                "price" -> it.value.jsonArray.map { v -> v.jsonPrimitive.double }
                else -> throw IllegalStateException("Unknown key ${it.key}")
            }
        }
        return map
    }
}