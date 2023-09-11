package kr.kro.tripsketch.dto

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserDto(
    @field:Size(min = 3, max = 50, message = "별명은 3자에서 50자 사이여야 합니다.")
    val nickname: String?,

    @field:Size(max = 500, message = "소개는 최대 500자까지 가능합니다.")
    val introduction: String?,

    val profileImageUrl: String?,

    val followersCount: Long? = null,
    val followingCount: Long? = null,

    val isAdmin: Boolean?,
)
