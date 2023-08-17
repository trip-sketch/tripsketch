package kr.kro.tripsketch.dto

data class KakaoLoginRequest(val code: String, val pushToken: String? = null)