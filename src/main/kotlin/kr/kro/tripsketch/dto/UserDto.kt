package kr.kro.tripsketch.dto

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Length

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserDto(
    @field:Email(message = "올바른 이메일 형식이어야 합니다.")
    @field:NotBlank(message = "이메일은 비워둘 수 없습니다.")
    val email: String?,

    @field:Size(min = 3, max = 50, message = "별명은 3자에서 50자 사이여야 합니다.")
    val nickname: String?,

    @field:Size(max = 500, message = "소개는 최대 500자까지 가능합니다.")
    val introduction: String?,

    @field:Length(min = 5, max = 500, message = "텍스트 길이는 5자 이상 500자 이하이어야 합니다.")
    val profileImageUrl: String?,

    val followersCount: Long? = null,
    val followingCount: Long? = null,

    val isAdmin: Boolean?,
)