package data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Grade(val mark: Int) {
    @SerialName("5")
    A(5),

    @SerialName("4")
    B(4),

    @SerialName("3")
    C(3),

    @SerialName("2")
    F(2);

    companion object {
        val list = listOf(A, B, C, F)
    }
}