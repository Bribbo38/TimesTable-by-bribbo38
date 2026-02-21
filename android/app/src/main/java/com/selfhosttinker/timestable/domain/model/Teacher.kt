package com.selfhosttinker.timestable.domain.model

data class Teacher(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = ""
) {
    val fullName: String get() = "$firstName $lastName".trim()
}
