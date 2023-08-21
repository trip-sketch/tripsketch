package kr.kro.tripsketch.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserDto(
    val email: String?,
    val nickname: String?,
    val introduction: String?,
    val profileImageUrl: String?,
    val followersCount: Long? = null,
    val followingCount: Long? = null
)
