package common

import kotlinx.serialization.Serializable

typealias ItemId = String

@Serializable
class Item<E>(
    val id: ItemId,
    val elem: E,
)