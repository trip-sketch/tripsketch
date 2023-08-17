package kr.kro.tripsketch.dto

data class KakaoRefreshRequest(val ourRefreshToken: String, val pushToken: String? = null)