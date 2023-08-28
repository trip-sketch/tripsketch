package kr.kro.tripsketch.domain

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Length
import org.jetbrains.annotations.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "users")
data class User(
    @Id val id: String? = null,

    @Indexed(unique = true)
    @field:Email(message = "이메일 형식이어야 합니다.")
    @field:NotBlank
    val email: String,

    @Indexed(unique = true)
    @field:NotBlank(message = "닉네임을 입력해주세요.")
    @field:Size(min = 3, max = 50, message = "2글자에서 50글자 사이만 가능합니다.")
    var nickname: String,

    @field:Size(max = 500, message = "500글자 이내로 가능합니다.")
    var introduction: String?,

    @field:Length(min = 5, max = 500, message = "텍스트 길이는 5자 이상 500자 이하이어야 합니다.")
    var profileImageUrl: String?,

    @field:NotNull
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @field:NotNull
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    var kakaoRefreshToken: String? = null,     // 카카오로부터 발급받은 refreshToken
    var ourRefreshToken: String? = null,    // 서비스 자체에서 발급한 refreshToken
    var expoPushToken: String? = null, // Notification을 위한 Expo Notification Token
)
