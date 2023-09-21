package kr.kro.tripsketch.domain

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 댓글 정보를 담은 도메인 클래스입니다.
 *
 * @property id 댓글 식별자 (Optional)
 * @property userId 사용자 이메일 (Required)
 * @property tripId 게시물 아이디 (Required)
 * @property parentId 부모 댓글의 식별자 (Optional)
 * @property content 댓글 내용 (Required, 최대 200자)
 * @property createdAt 댓글 생성 일시 (기본값: 현재 일시)
 * @property updatedAt 댓글 수정 일시 (기본값: 현재 일시)
 * @property likedBy 댓글을 좋아하는 사용자 목록 (기본값: 빈 Set)
 * @property replyToUserId 답글 대상 사용자 이메일 (Optional)
 * @property children 자식 댓글 목록 (기본값: 빈 리스트)
 * @property isDeleted 댓글 삭제 여부 (기본값: false)
 * @property isLiked 댓글 좋아요 여부 (기본값: false)
 * @property numberOfLikes 댓글 좋아요 수 (기본값: 0)
 * @author BYEONGUK KO
 */
@Document(collection = "comments")
data class Comment(
    @Id val id: String? = null,

    @field:NotBlank(message = "User email must not be blank")
    @Indexed(unique = true)
    val userId: String? = null,

    @field:NotBlank(message = "User tripId must not be blank")
    @Indexed(unique = true)
    val tripId: String,

    val parentId: String? = null,

    @field:NotBlank(message = "User content must not be blank")
    @field:Size(max = 200, message = "댓글 내용(content)은 200자 이하로 입력해주세요.")
    val content: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val likedBy: MutableSet<String> = mutableSetOf(),
    val replyToUserId: String? = null,
    val children: MutableList<Comment> = mutableListOf(),
    val isDeleted: Boolean = false,
    val isLiked: Boolean = false,
    var numberOfLikes: Int = 0,
)
