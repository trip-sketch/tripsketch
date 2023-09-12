package kr.kro.tripsketch.dto

import kr.kro.tripsketch.domain.Notification

data class ResponseFormat(
    val currentPage: Int,
    val posts: List<Notification>,
    val postsPerPage: Int,
    val totalPage: Int
)