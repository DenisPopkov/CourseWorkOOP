package ru.popkov.coursework.application

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
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