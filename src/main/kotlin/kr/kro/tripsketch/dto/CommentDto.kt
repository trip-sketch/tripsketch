package kr.kro.tripsketch.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * 댓글 정보를 담은 DTO 클래스입니다.
 *
 * @property id 댓글 식별자 (Optional)
 * @property userNickName 유저 닉네임 (Required)
 * @property userProfileUrl 유저 프로필 이미지 URL (Required)
 * @property tripId 게시물 아이디 (Required)
 * @property parentId 부모 댓글의 식별자 (Optional)
 * @property content 댓글 내용 (Required, 최대 200자)
 * @property createdAt 댓글 생성 일시 (기본값: 현재 일시)
 * @property updatedAt 댓글 수정 일시 (기본값: 현재 일시)
 * @property replyToNickname 답글 대상 닉네임 (Optional)
 * @property isDeleted 댓글 삭제 여부 (기본값: false)
 * @property isLiked 댓글 좋아요 여부 (기본값: false)
 * @property numberOfLikes 댓글 좋아요 수 (기본값: 0)
 * @property children 자식 댓글 목록 (기본값: 빈 리스트)
 * @author BYEONGUK KO
 */
data class CommentDto(
    val id: String? = null,

    @field:NotBlank(message = "유저 닉네임(userNickName)은 필수 항목입니다.")
    val userNickName: String,

    @field:NotBlank(message = "유저 프로필(userProfileUrl)은 필수 항목입니다.")
    val userProfileUrl: String,

    @field:NotBlank(message = "게시물 아이디(tripId)는 필수 항목입니다.")
    val tripId: String,

    val parentId: String? = null,

    @field:NotBlank(message = "댓글 내용(content)은 필수 항목입니다.")
    @field:Size(max = 200, message = "댓글 내용(content)은 200자 이하로 입력해주세요.")
    val content: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val replyToNickname: String? = null,
    val isDeleted: Boolean = false,
    var isLiked: Boolean = false,
    var numberOfLikes: Int = 0,
    val children: MutableList<CommentDto> = mutableListOf(),
)
