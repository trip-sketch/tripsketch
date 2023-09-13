package kr.kro.tripsketch.dto

import kr.kro.tripsketch.domain.Notification

data class ResponseFormat(
    val currentPage: Int,
    val notifications: List<Notification>,
    val notificationsPerPage: Int,
    val totalPage: Int
)