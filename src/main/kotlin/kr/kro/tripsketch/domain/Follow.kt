package kr.kro.tripsketch.domain

import jakarta.validation.constraints.NotBlank
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * 구독 정보를 나타내는 데이터 클래스입니다.
 *
 * @author Hojun Song
 */
@Document(collection = "follows")
data class Follow(
    @Id val id: String? = null,

    @field:NotBlank(message = "비워둘 수 없습니다.")
    val follower: String,

    @field:NotBlank(message = "비워둘 수 없습니다.")
    val following: String,
)
