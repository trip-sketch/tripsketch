package kr.kro.tripsketch.domain

import kr.kro.tripsketch.dto.UserDto
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "users")
data class User(
    @Id val id: String? = null,

    @Indexed(unique = true)
    val email: String,

    @Indexed(unique = true)
    var nickname: String,

    var introduction: String?,
    var profileImageUrl: String?,

    var createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    var kakaoRefreshToken: String? = null,     // 카카오로부터 발급받은 refreshToken
    var ourRefreshToken: String? = null,    // 서비스 자체에서 발급한 refreshToken
    var expoPushToken: String? = null, //Notification을 위한 Expo Notification Token
)

fun toDto(user: User): UserDto {
    return UserDto(
        id = user.id,
        email = user.email,
        nickname = user.nickname,
        introduction = user.introduction,
        profileImageUrl = user.profileImageUrl
    )
}
