package ru.popkov.transport.timer.server.application

import io.ktor.server.plugins.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

sealed class ServiceResult<out T : Any> {
    @Serializable
    data class Success<out T : Any>(val data: T) : ServiceResult<T>()
}

@ExperimentalSerializationApi
class DynamicLookupSerializer : SerializationStrategy<Any> {
    override val descriptor: SerialDescriptor = ContextualSerializer(Any::class, null, emptyArray()).descriptor

    @OptIn(InternalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Any) {
        val actualSerializer = encoder.serializersModule.getContextual(value::class) ?: value::class.serializer()
        encoder.encodeSerializableValue(actualSerializer as KSerializer<Any>, value)
    }
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Any> serializeServiceResult(x: ServiceResult<T>) = when (x) {
    is ServiceResult.Success -> Json.encodeToString(serializer = DynamicLookupSerializer(), value = x.data)
}

inline fun <reified E : Enum<E>> enumValueOrError(value: String): E {
    return try {
        enumValueOf(value)
    } catch (e: java.lang.IllegalArgumentException) {
        throw BadRequestException("Does Not Support : $value.")
    }
}

inline fun <reified T> stringToTypeOrError(value: String): T {
    val v: T = when (T::class) {
        Any::class -> value as T
        Int::class -> value.toInt() as T
        String::class -> value as T
        Boolean::class -> value.toBoolean() as T
        Double::class -> value.toDouble() as T
        else -> {
            if (T::class.java.isEnum) {
                @Suppress("UPPER_BOUND_VIOLATED")
                return enumValueOrError<T>(value)
            } else {
                throw NotImplementedError("Not implemented for type:${T::class.qualifiedName}")
            }
        }
    }
    return v
}