package kr.kro.tripsketch.comment

import kr.kro.tripsketch.comment.dtos.CommentUpdateDto
import kr.kro.tripsketch.comment.dtos.CommentChildrenCreateDto
import kr.kro.tripsketch.comment.dtos.CommentDto
import kr.kro.tripsketch.comment.dtos.CommentCreateDto
import kr.kro.tripsketch.dto.*
import kr.kro.tripsketch.exceptions.ForbiddenException
import kr.kro.tripsketch.repositories.TripRepository
import kr.kro.tripsketch.repositories.UserRepository
import kr.kro.tripsketch.services.NotificationService
import kr.kro.tripsketch.services.UserService
import kr.kro.tripsketch.utils.EnvLoader
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.math.min

/**
 * CommentService 클래스
 *
 * 주요 기능:
 * - 댓글 관련 비즈니스 로직을 담당합니다.
 *
 * @property commentRepository 댓글 관련 데이터의 CRUD를 처리하는 레포지토리
 * @property userRepository 사용자 관련 데이터의 CRUD를 처리하는 레포지토리
 * @property userService 사용자 정보 관련 비즈니스 로직을 담당하는 서비스
 * @property notificationService 알림 서비스
 * @property tripRepository 여행 관련 데이터의 CRUD를 처리하는 레포지토리
 * @author BYEONGUK KO
 */
@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val notificationService: NotificationService,
    private val tripRepository: TripRepository,
) {

    /**
     * 페이지네이션을 적용하여 모든 댓글을 가져옵니다.
     *
     * @param page 현재 페이지 번호
     * @param pageSize 페이지당 댓글 수
     * @return 페이지네이션된 댓글 목록과 페이지 관련 정보
     */
    fun getAllCommentsWithPagination(page: Int, pageSize: Int): Map<String, Any> {
        val commentsPage = commentRepository.findAll()
        val commentsList = commentsPage.map { fromComment(it, userService) }

        return paginateComments(commentsList, page, pageSize)
    }

    /**
     * 게스트가 특정 여행의 댓글 목록을 가져옵니다.
     *
     * @param tripId 조회할 여행의 ID
     * @return 댓글 목록
     * @throws IllegalArgumentException 해당 게시글이 존재하지 않거나 접근 권한이 없을 경우 발생
     */
    fun getCommentsGuestByTripId(tripId: String): List<CommentDto> {
        val trip = tripRepository.findByIdAndIsHiddenIsFalse(tripId)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        if (trip.isPublic == false) {
            throw IllegalArgumentException("해당 게시글의 접근 권한이 없습니다. ")
        }
        val comments = commentRepository.findAllByTripId(tripId)
        return comments.map { fromComment(it, userService) }
    }

    /**
     * 관리자 권한으로 특정 여행의 댓글 목록을 가져옵니다.
     *
     * @param tripId 조회할 여행의 ID
     * @return 댓글 목록
     * @throws IllegalArgumentException 해당 게시글이 존재하지 않을 경우 발생
     */
    fun getCommentsAdminByTripId(tripId: String): List<CommentDto> {
        tripRepository.findByIdAndIsHiddenIsFalse(tripId)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        val comments = commentRepository.findAllByTripId(tripId)
        return comments.map { fromComment(it, userService) }
    }

    /**
     * 특정 여행에 속한 모든 댓글을 영구 삭제합니다.
     *
     * @param tripId 삭제할 댓글들이 속한 여행의 ID
     */
    fun deleteAllCommentsAdminByTripId(tripId: String) {
        val commentsToDelete = getCommentsAdminByTripId(tripId)

        commentsToDelete.forEach { commentDto ->
            val commentId = commentDto.id
            commentId?.let { commentRepository.deleteById(it) }
        }
    }

    /**
     * 로그인한 유저가 특정 여행에서 좋아요를 누른 댓글 목록을 조회합니다.
     *
     * @param memberId 조회할 유저의 ID
     * @param tripId 조회할 여행의 ID
     * @return 좋아요를 누른 댓글 목록
     * @throws IllegalArgumentException 해당 게시글이 존재하지 않을 경우 발생
     */
    fun getIsLikedByMemberIdForTrip(memberId: Long, tripId: String): List<CommentDto> {
        val updatedComments = isLikedByTokenForComments(memberId, tripId)

        return updatedComments.map { fromComment(it, userService) }
    }

    /**
     * 특정 유저가 특정 여행의 댓글들에 대한 좋아요 정보를 반환합니다.
     *
     * @param memberId 좋아요 정보를 업데이트할 유저의 ID
     * @param tripId 좋아요 정보를 업데이트할 여행의 ID
     * @return 좋아요 정보가 업데이트된 댓글 목록
     */
    private fun isLikedByTokenForComments(memberId: Long, tripId: String): List<Comment> {
        tripRepository.findByIdAndIsHiddenIsFalse(tripId)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")

        val comments = commentRepository.findAllByTripId(tripId)
        val commenter =
            userService.findUserByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일의 사용자 존재하지 않습니다.")
        return comments.map { comment ->
            val isLiked = comment.likedBy.contains(commenter.id)
            val updatedComment = comment.copy(isLiked = isLiked)

            val updatedChildren = comment.children.map { child ->
                child.copy(isLiked = child.likedBy.contains(commenter.id))
            }.toMutableList()

            updatedComment.copy(children = updatedChildren)
        }
    }

    /**
     * 새로운 댓글을 생성합니다.
     *
     * @param memberId 댓글을 작성한 회원의 ID
     * @param commentCreateDto 생성할 댓글의 정보를 담은 DTO
     * @return CommentDto 생성된 댓글의 정보를 담은 DTO
     * @throws IllegalArgumentException 해당 게시글이 존재하지 않거나 사용자가 존재하지 않을 경우 발생
     */
    fun createComment(memberId: Long, commentCreateDto: CommentCreateDto): CommentDto {
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(commentCreateDto.tripId)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        val commenter =
            userService.findUserByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일의 사용자 존재하지 않습니다.")
        val comment = Comment(
            userId = commenter.id,
            tripId = commentCreateDto.tripId,
            content = commentCreateDto.content,
        )
        val createdComment = commentRepository.save(comment)

        val tripUserId = findTrip.userId
        // 알림 적용
        if (tripUserId != commenter.id && findTrip.isPublic == true) {
            notificationService.sendPushNotification(
                listOf(tripUserId),
                "새로운 여행의 시작, 트립스케치",
                "${commenter.nickname} 님이 댓글을 남겼습니다. ",
                nickname = commenter.nickname,
                profileUrl = commenter.profileImageUrl,
                tripId = comment.tripId,
                senderId = commenter.id,
                commentId = comment.id,
                content = commentCreateDto.content,
            )
        }
        return fromComment(createdComment, userService)
    }

    /**
     * 댓글의 자식 댓글을 생성합니다. (대댓글 생성)
     *
     * @param memberId 댓글을 작성한 회원의 ID
     * @param parentId 부모 댓글의 ID
     * @param commentChildrenCreateDto 생성할 자식 댓글의 정보를 담은 DTO
     * @return CommentDto 생성된 자식 댓글의 정보를 담은 부모 댓글의 DTO
     * @throws IllegalArgumentException 해당 게시글이 존재하지 않거나 사용자가 존재하지 않을 경우 발생
     */

    fun createChildrenComment(
        memberId: Long,
        parentId: String,
        commentChildrenCreateDto: CommentChildrenCreateDto,
    ): CommentDto {
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(commentChildrenCreateDto.tripId)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        val commenter = userRepository.findByMemberId(memberId) ?: throw IllegalArgumentException("해당 사용자가 존재하지 않습니다.")

        val parentComment: Comment = parentId.let {
            commentRepository.findById(it).orElse(null)
        }
            ?: throw IllegalArgumentException("해당 parentId 댓글은 존재하지 않습니다.")

        val mentionedUser =
            userRepository.findByNickname(commentChildrenCreateDto.replyToNickname)
                ?: throw IllegalArgumentException("해당 언급 된 사용자 존재하지 않습니다.")

        val childComment = Comment(
            id = ObjectId().toString(), // 새로운 ObjectId 생성
            userId = commenter.id,
            tripId = commentChildrenCreateDto.tripId,
            parentId = parentId,
            content = commentChildrenCreateDto.content,
            replyToUserId = mentionedUser.id,
        )

        parentComment.children.add(childComment)
        val createdComment = commentRepository.save(parentComment)

        val tripUserId = findTrip.userId
        // 알림 적용
        val notificationRecipients = mutableListOf<String>()

        if (tripUserId != commenter.id) {
            notificationRecipients.add(tripUserId)
        }

        if (parentComment.userId != commenter.id) {
            notificationRecipients.add(parentComment.userId!!)
        }

        if (mentionedUser.id != commenter.id) {
            notificationRecipients.add(mentionedUser.id!!)
        }

        if (notificationRecipients.isNotEmpty() && findTrip.isPublic == true) {
            notificationService.sendPushNotification(
                notificationRecipients,
                "새로운 여행의 시작, 트립스케치",
                "${commenter.nickname} 님이 댓글을 남겼습니다. ",
                nickname = commenter.nickname,
                profileUrl = commenter.profileImageUrl,
                tripId = childComment.tripId,
                parentId = childComment.parentId,
                senderId = commenter.id,
                commentId = childComment.id,
                content = commentChildrenCreateDto.content,
            )
        }
        return fromComment(createdComment, userService)
    }

    /**
     * 댓글을 업데이트합니다.
     *
     * @param memberId 댓글을 작성한 회원의 ID
     * @param id 업데이트할 댓글의 ID
     * @param commentUpdateDto 업데이트할 댓글의 정보를 담은 DTO
     * @return CommentDto 업데이트된 댓글의 정보를 담은 DTO
     * @throws IllegalArgumentException 해당 ID의 댓글이 존재하지 않거나 사용자가 존재하지 않을 경우 발생
     * @throws ForbiddenException 댓글 작성자 또는 관리자가 아닌 사용자가 업데이트를 시도할 경우 발생
     */
    fun updateComment(memberId: Long, id: String, commentUpdateDto: CommentUpdateDto): CommentDto {
        val comment =
            commentRepository.findById(id).orElse(null) ?: throw IllegalArgumentException("해당 id 댓글은 존재하지 않습니다.")
        tripRepository.findByIdAndIsHiddenIsFalse(comment.tripId)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        val commenter =
            userRepository.findByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일의 사용자 존재하지 않습니다.")

        if (comment.userId != commenter.id) {
            throw ForbiddenException("해당 사용자만 접근 가능합니다.")
        }
        if (comment.isDeleted) {
            throw ForbiddenException("삭제 된 댓글은 수정 할 수 없습니다.")
        }
        val updatedTime = LocalDateTime.now()
        val updatedComment = comment.copy(
            content = commentUpdateDto.content ?: comment.content,
            updatedAt = updatedTime,
        )

        val savedComment = commentRepository.save(updatedComment)

        return fromComment(savedComment, userService)
    }

    /**
     * 자식 댓글을 업데이트합니다. (대댓글)
     *
     * @param memberId 댓글을 작성한 회원의 ID
     * @param parentId 부모 댓글의 ID
     * @param id 업데이트할 자식 댓글의 ID
     * @param commentUpdateDto 업데이트할 자식 댓글의 정보를 담은 DTO
     * @return CommentDto 업데이트된 자식 댓글의 정보를 담은 부모 댓글의 DTO
     * @throws IllegalArgumentException 해당 ID의 댓글이 존재하지 않거나 사용자가 존재하지 않을 경우 발생
     * @throws ForbiddenException 댓글 작성자 또는 관리자가 아닌 사용자가 업데이트를 시도할 경우 발생
     */
    fun updateChildrenComment(
        memberId: Long,
        parentId: String,
        id: String,
        commentUpdateDto: CommentUpdateDto,
    ): CommentDto {
        val parentComment = commentRepository.findById(parentId).orElse(null)
            ?: throw IllegalArgumentException("해당 parentId 댓글은 존재하지 않습니다.")
        tripRepository.findByIdAndIsHiddenIsFalse(parentComment.tripId)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")

        val childCommentIndex = parentComment.children.indexOfFirst { it.id == id }
        if (childCommentIndex == -1) {
            throw IllegalArgumentException("해당 id에 대응하는 댓글이 children 존재하지 않습니다.")
        }

        val commenter =
            userRepository.findByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일의 사용자 존재하지 않습니다.")

        if (parentComment.children[childCommentIndex].userId != commenter.id) {
            throw ForbiddenException("해당 사용자만 접근 가능합니다.")
        }

        if (parentComment.children[childCommentIndex].isDeleted) {
            throw ForbiddenException("삭제 된 댓글은 수정 할 수 없습니다.")
        }

        val updatedTime = LocalDateTime.now()
        val updatedChildComment = parentComment.children[childCommentIndex].copy(
            content = commentUpdateDto.content ?: parentComment.children[childCommentIndex].content,
            updatedAt = updatedTime,
        )

        parentComment.children[childCommentIndex] = updatedChildComment
        val savedParentComment = commentRepository.save(parentComment)

        return fromComment(savedParentComment, userService)
    }

    /**
     * 댓글을 삭제합니다.
     *
     * @param memberId 댓글을 작성한 회원의 ID
     * @param id 삭제할 댓글의 ID
     * @throws IllegalArgumentException 해당 ID의 댓글이 존재하지 않거나 사용자가 존재하지 않을 경우 발생
     * @throws ForbiddenException 댓글 작성자 또는 관리자가 아닌 사용자가 삭제를 시도할 경우 발생
     */
    fun deleteComment(memberId: Long, id: String) {
        val comment = commentRepository.findById(id).orElse(null)
            ?: throw IllegalArgumentException("해당 id 댓글은 존재하지 않습니다.")
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(comment.tripId)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")

        val commenter =
            userRepository.findByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일의 사용자 존재하지 않습니다.")

        val adminIdsStrings = EnvLoader.getProperty("ADMIN_IDS")?.split(",") ?: listOf()
        val adminIds = adminIdsStrings.mapNotNull { it.toLongOrNull() }

        if (memberId in adminIds) {
            val deletedComment = comment.copy(
                content = "삭제 된 댓글입니다.",
                isDeleted = true,
                likedBy = mutableSetOf(),
                numberOfLikes = 0,
            )
            commentRepository.save(deletedComment)
            return
        }

        if (comment.isDeleted) {
            throw ForbiddenException("이미 삭제 된 댓글 입니다.")
        }

        if (findTrip.isPublic == false && findTrip.userId != commenter.id) {
            throw ForbiddenException("해당 댓글 접근 권한이 없습니다.")
        }
        if (comment.userId != commenter.id) {
            throw ForbiddenException("해당 사용자만 접근 가능합니다.")
        }
        // Soft delete 처리
        val deletedComment = comment.copy(
            content = "삭제 된 댓글입니다.",
            isDeleted = true,
            likedBy = mutableSetOf(),
            numberOfLikes = 0,
        )
        commentRepository.save(deletedComment)
    }

    /**
     * 자식 댓글을 삭제합니다. (대댓글 삭제)
     *
     * @param memberId 댓글을 작성한 회원의 ID
     * @param parentId 부모 댓글의 ID
     * @param id 삭제할 자식 댓글의 ID
     * @throws IllegalArgumentException 해당 ID의 댓글이 존재하지 않거나 사용자가 존재하지 않을 경우 발생
     * @throws ForbiddenException 댓글 작성자 또는 관리자가 아닌 사용자가 삭제를 시도할 경우 발생
     */
    fun deleteChildrenComment(memberId: Long, parentId: String, id: String) {
        val parentComment = commentRepository.findById(parentId).orElse(null)
            ?: throw IllegalArgumentException("해당 parentId 댓글은 존재하지 않습니다.")

        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(parentComment.tripId)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")

        val childCommentIndex = parentComment.children.indexOfFirst { it.id == id }
        if (childCommentIndex == -1) {
            throw IllegalArgumentException("해당 id에 대응하는 댓글이 children에 존재하지 않습니다.")
        }

        if (parentComment.children[childCommentIndex].isDeleted) {
            throw ForbiddenException("이미 삭제 된 댓글 입니다.")
        }

        val commenter =
            userRepository.findByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일의 사용자 존재하지 않습니다.")

        val adminIdsStrings = EnvLoader.getProperty("ADMIN_IDS")?.split(",") ?: listOf()
        val adminIds = adminIdsStrings.mapNotNull { it.toLongOrNull() }

        if (memberId in adminIds) {
            // Soft delete 처리
            val deletedChildComment =
                parentComment.children[childCommentIndex].copy(
                    content = "삭제 된 댓글입니다.",
                    isDeleted = true,
                    likedBy = mutableSetOf(),
                    numberOfLikes = 0,
                    replyToUserId = "",
                )
            parentComment.children[childCommentIndex] = deletedChildComment
            commentRepository.save(parentComment)
            return
        }

        if (findTrip.isPublic == false && findTrip.userId != commenter.id) {
            throw ForbiddenException("해당 댓글 접근 권한이 없습니다.")
        }

        if (parentComment.children[childCommentIndex].userId != commenter.id) {
            throw ForbiddenException("해당 사용자만 접근 가능합니다.")
        }

        // Soft delete 처리
        val deletedChildComment =
            parentComment.children[childCommentIndex].copy(
                content = "삭제 된 댓글입니다.",
                isDeleted = true,
                likedBy = mutableSetOf(),
                numberOfLikes = 0,
                replyToUserId = "",
            )
        parentComment.children[childCommentIndex] = deletedChildComment
        commentRepository.save(parentComment)
    }

    /**
     * 댓글에 좋아요, 좋아요 취소를 토글 형식으로 요청합니다.
     *
     * @param memberId 좋아요를 누른 회원의 ID
     * @param id 좋아요를 토글할 댓글의 ID
     * @return CommentDto 좋아요가 토글된 댓글의 정보를 담은 DTO
     * @throws IllegalArgumentException 해당 ID의 댓글이 존재하지 않거나 사용자가 존재하지 않을 경우 발생
     * @throws ForbiddenException 비공개 게시글이면 댓글 작성자가 아닌 사용자가 좋아요를 시도할 경우, 삭제 된 댓글의 경우 발생
     */
    fun toggleLikeComment(memberId: Long, id: String): CommentDto {
        val commenter =
            userRepository.findByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일의 사용자 존재하지 않습니다.")

        val userId = commenter.id

        val comment = commentRepository.findById(id).orElse(null)
            ?: throw IllegalArgumentException("해당 id 댓글은 존재하지 않습니다.")

        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(comment.tripId)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")

        if (findTrip.isPublic == false && findTrip.userId != commenter.id) {
            throw ForbiddenException("해당 댓글 접근 권한이 없습니다.")
        }

        if (comment.isDeleted) {
            throw ForbiddenException("삭제 된 댓글은 좋아요 할 수 없습니다.")
        }
        if (comment.likedBy.contains(userId)) {
            comment.likedBy.remove(userId) // 이미 좋아요를 누른 경우 좋아요 취소
            comment.numberOfLikes -= 1
        } else {
            userId?.let { comment.likedBy.add(it) } // 좋아요 추가
            comment.numberOfLikes += 1

            // 자신의 댓글에 좋아요를 남기지 않았을 경우
            if (userId != comment.userId) {
                // 알림 적용
                notificationService.sendPushNotification(
                    listOf(comment.userId!!),
                    "새로운 여행의 시작, 트립스케치",
                    "${commenter.nickname} 님이 회원님의 댓글을 좋아합니다.",
                    nickname = commenter.nickname,
                    profileUrl = commenter.profileImageUrl,
                    tripId = comment.tripId,
                    senderId = commenter.id,
                    commentId = comment.id,
                    content = comment.content,
                )
            }
        }

        val savedComment = commentRepository.save(comment)
        return fromComment(savedComment, userService)
    }

    /**
     * 자식 댓글에 좋아요, 좋아요 취소를 토글 형식으로 요청합니다. (대댓글)
     *
     * @param memberId 좋아요를 누른 회원의 ID
     * @param parentId 부모 댓글의 ID
     * @param id 좋아요를 토글할 자식 댓글의 ID
     * @return 좋아요가 토글된 자식 댓글의 정보를 담은 DTO
     * @throws IllegalArgumentException 해당 ID의 댓글이 존재하지 않거나 사용자가 존재하지 않을 경우 발생
     * @throws ForbiddenException 비공개 게시글이면 댓글 작성자가 아닌 사용자가 좋아요를 시도할 경우, 삭제 된 댓글의 경우 발생
     */
    fun toggleLikeChildrenComment(memberId: Long, parentId: String, id: String): CommentDto {
        val parentComment = commentRepository.findById(parentId).orElse(null)
            ?: throw IllegalArgumentException("해당 parentId 댓글은 존재하지 않습니다.")
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(parentComment.tripId)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        val commenter =
            userRepository.findByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일의 사용자 존재하지 않습니다.")

        if (findTrip.isPublic == false && findTrip.userId != commenter.id) {
            throw ForbiddenException("해당 댓글 접근 권한이 없습니다.")
        }

        val userId = commenter.id
        val childCommentIndex = parentComment.children.indexOfFirst { it.id == id }
        if (childCommentIndex == -1) {
            throw IllegalArgumentException("해당 id에 대응하는 댓글이 children에 존재하지 않습니다.")
        }
        if (parentComment.children[childCommentIndex].isDeleted) {
            throw ForbiddenException("삭제 된 댓글은 좋아요 할 수 없습니다.")
        }

        val childComment = parentComment.children[childCommentIndex]
        if (childComment.likedBy.contains(userId)) {
            childComment.likedBy.remove(userId) // 이미 좋아요를 누른 경우 좋아요 취소
            childComment.numberOfLikes -= 1
        } else {
            userId?.let { childComment.likedBy.add(it) } // 좋아요 추가
            childComment.numberOfLikes += 1
            // 자신의 댓글에 좋아요를 남기지 않았을 경우
            if (userId != childComment.userId) {
                // 알림 적용
                notificationService.sendPushNotification(
                    listOf(childComment.userId!!),
                    "새로운 여행의 시작, 트립스케치",
                    "${commenter.nickname} 님이 회원님의 댓글을 좋아합니다.",
                    nickname = commenter.nickname,
                    profileUrl = commenter.profileImageUrl,
                    tripId = childComment.tripId,
                    parentId = childComment.parentId,
                    senderId = commenter.id,
                    commentId = childComment.id,
                    content = childComment.content,
                )
            }
        }

        val savedParentComment = commentRepository.save(parentComment)
        return fromComment(savedParentComment, userService)
    }

    companion object {

        /**
         * Comment 객체로부터 CommentDto를 생성합니다.
         *
         * @param comment : Comment 객체
         * @param userService : UserService 객체
         * @return 생성된 CommentDto
         */
        fun fromComment(comment: Comment, userService: UserService): CommentDto {
            val commenter = comment.userId?.let { userService.findUserById(it) }

            val commenterProfile = commenter?.let {
                UserProfileDto(
                    nickname = it.nickname,
                    introduction = it.introduction ?: "",
                    profileImageUrl = it.profileImageUrl ?: "",
                )
            }

            val mentionedUser = comment.replyToUserId?.let { userService.findUserById(it) }

            val mentionedUserNickname = mentionedUser?.nickname

            return CommentDto(
                id = comment.id,
                userNickName = commenterProfile?.nickname ?: "알 수 없는 사용자", // 사용자가 없을 경우 대비
                userProfileUrl = commenterProfile?.profileImageUrl
                    ?: "https://ax6izwmsuv9c.objectstorage.ap-osaka-1.oci.customer-oci.com/n/ax6izwmsuv9c/b/tripsketch/o/profile.png",
                tripId = comment.tripId,
                parentId = comment.parentId,
                content = comment.content,
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt,
                replyToNickname = mentionedUserNickname,
                isDeleted = comment.isDeleted,
                isLiked = comment.isLiked,
                numberOfLikes = comment.numberOfLikes,
                children = comment.children.map { fromComment(it, userService) }.toMutableList(),
            )
        }
    }
}

/**
 * 댓글 목록을 페이지네이션하여 반환합니다.
 *
 * @param comments 댓글 목록
 * @param page 페이지 번호
 * @param pageSize 페이지당 댓글 수
 * @return 페이지네이션된 댓글 목록과 페이지 관련 정보를 담은 Map
 */
fun paginateComments(comments: List<CommentDto>, page: Int, pageSize: Int): Map<String, Any> {
    val paginatedComments = mutableListOf<CommentDto>()
    val formattedComments = formattingCommentsWithChildren(comments)

    val totalComments = formattedComments.size
    val totalPage = (totalComments + pageSize - 1) / pageSize

    val sortedComments = formattedComments.sortedByDescending { it.updatedAt }

    val startIndex = (page - 1) * pageSize
    val endIndex = min(startIndex + pageSize, totalComments)

    for (i in startIndex until endIndex) {
        paginatedComments.add(sortedComments[i])
    }

    return mapOf(
        "comments" to paginatedComments,
        "currentPage" to page,
        "totalPage" to totalPage,
        "commentsPerPage" to pageSize,
    )
}

/**
 * 댓글 목록을 형식에 맞게 정리합니다.
 *
 * 각 댓글은 그 자신을 포함한 모든 대댓글을 포함하는 새로운 리스트로 구성됩니다.
 * 댓글의 자식(대댓글)이 있는 경우, 그 자식들이 해당 댓글 뒤에 연이어 추가됩니다.
 *
 * @param comments 댓글 목록
 * @return 포맷팅된 댓글 목록
 */
fun formattingCommentsWithChildren(comments: List<CommentDto>): List<CommentDto> {
    val commentsList = mutableListOf<CommentDto>()

    comments.forEach { comment ->
        val updatedComment = comment.copy(children = mutableListOf())
        commentsList.add(updatedComment)

        // 대댓글 추가
        comment.children.let { children ->
            children.forEach { childComment ->
                commentsList.add(childComment)
            }
        }
    }

    return commentsList
}
