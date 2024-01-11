package kr.kro.tripsketch.user.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.jetbrains.annotations.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 사용자 정보를 나타내는 데이터 클래스입니다.
 *
 * @property id 사용자 ID
 * @property memberId 사용자 회원번호
 * @property nickname 사용자 닉네임
 * @property introduction 사용자 소개
 * @property profileImageUrl 사용자 프로필 이미지 URL
 * @property createdAt 계정 생성 일시
 * @property updatedAt 계정 업데이트 일시
 * @property kakaoRefreshToken 카카오 API의 Refresh Token
 * @property ourRefreshToken 자체 서비스의 Refresh Token
 * @property expoPushToken Expo 푸시 알림 토큰
 * @author Hojun Song
 */
@Document(collection = "users")
data class User(
    @Id val id: String? = null,

    @Indexed(unique = true)
    @field:NotBlank
    var memberId: Long,

    @Indexed(unique = true)
    @field:NotBlank(message = "닉네임을 입력해주세요.")
    @field:Size(min = 3, max = 50, message = "2글자에서 50글자 사이만 가능합니다.")
    var nickname: String,

    @field:Size(max = 500, message = "500글자 이내로 가능합니다.")
    var introduction: String?,

    var profileImageUrl: String?,

    @field:NotNull
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @field:NotNull
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    var kakaoRefreshToken: String? = null,
    var ourRefreshToken: String? = null,
    var expoPushToken: String? = null,
) {
    fun updateLastLogin() {
        this.updatedAt = LocalDateTime.now()
    }
}
