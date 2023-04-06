package ru.popkov.transport.timer.server.application

import io.ktor.server.plugins.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

sealed class ServiceResult<out T : Any> {
    @Serializable
    data class Success<out T : Any>(val data: T) : ServiceResult<T>()
}

inline fun <reified T : Any> serializeServiceResult(x: ServiceResult<T>): Pair<String, KClass<T>> = when (x) {
    is ServiceResult.Success -> {
        Pair(Json.encodeToString(x.data), T::class)
    }
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