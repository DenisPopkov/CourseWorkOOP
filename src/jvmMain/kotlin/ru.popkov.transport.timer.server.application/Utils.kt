package ru.popkov.transport.timer.server.application

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

sealed class ServiceResult<out T : Any> {
    @Serializable
    data class Success<out T : Any>(val data: T) : ServiceResult<T>()
    data class Error<out T : Any>(val error: T) : ServiceResult<T>()
}

inline fun <reified T : Any> serializeServiceResult(x: ServiceResult<T>): Pair<String, KClass<T>> = when (x) {
    is ServiceResult.Success -> Pair(Json.encodeToString(x.data), T::class)
    is ServiceResult.Error -> Pair("Error occurred with ${T::class}", T::class)
}