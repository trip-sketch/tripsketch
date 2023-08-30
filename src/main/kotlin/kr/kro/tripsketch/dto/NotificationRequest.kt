package kr.kro.tripsketch.dto

data class NotificationRequest(
    val email: String,
    val title: String,
    val body: String
)