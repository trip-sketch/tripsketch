package kr.kro.tripsketch.dto

data class NotificationRequest(
    val expoPushToken: String,
    val title: String,
    val body: String
)