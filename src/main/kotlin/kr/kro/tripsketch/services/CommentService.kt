package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Comment
import kr.kro.tripsketch.dto.*
import kr.kro.tripsketch.exceptions.ForbiddenException
import kr.kro.tripsketch.repositories.CommentRepository
import kr.kro.tripsketch.repositories.TripRepository
import kr.kro.tripsketch.repositories.UserRepository
import kr.kro.tripsketch.utils.EnvLoader
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val notificationService: NotificationService,
    private val tripRepository: TripRepository,
) {

    fun getAllComments(pageable: Pageable): Page<CommentDto> {
        return commentRepository.findAll(pageable).map { fromComment(it, userService) }
    }

    fun getAllCommentsWithPagination(page: Int, pageSize: Int): Map<String, Any> {
        val commentsPage = commentRepository.findAll()
        val commentsList = commentsPage.map { fromComment(it, userService) }

        return paginateComments(commentsList, page, pageSize)
    }


    fun getCommentsGuestByTripId(tripId: String): List<CommentDto> {
        val trip = tripRepository.findByIdAndIsHiddenIsFalse(tripId)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        if (trip.isPublic == false) {
            throw IllegalArgumentException("해당 게시글의 접근 권한이 없습니다. ")
        }
        val comments = commentRepository.findAllByTripId(tripId)
        return comments.map { fromComment(it, userService) }
    }

    fun getCommentsAdminByTripId(tripId: String): List<CommentDto> {
        val trip = tripRepository.findByIdAndIsHiddenIsFalse(tripId)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        val comments = commentRepository.findAllByTripId(tripId)
        return comments.map { fromComment(it, userService) }
    }

//    fun getCommentsByUserId(userId: String): List<CommentDto> {
//        return commentRepository.findAllByUserId(userId).map { fromComment(it, userService) }
//    }
//
//    fun getActiveComments(): List<CommentDto> {
//        return commentRepository.findAllByIsDeletedFalse().map { fromComment(it, userService) }
//    }
//
//    fun getLikedCommentsByUserId(userId: String): List<CommentDto> {
//        return commentRepository.findAllByLikedByContains(userId).map { fromComment(it, userService) }
//    }
//
//    fun getChildCommentsByParentId(parentId: String): List<CommentDto> {
//        return commentRepository.findAllByParentId(parentId).map { fromComment(it, userService) }
//    }


    /** 로그인 한 유저가 좋아요가 있는 댓글 조회 */
    fun getIsLikedByMemberIdForTrip(memberId: Long, tripId: String): List<CommentDto> {
        val updatedComments = isLikedByTokenForComments(memberId, tripId)

        return updatedComments.map { fromComment(it, userService) }
    }

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

    // 트립아이디 조회 하여 널이 아닐경우에만 글을 쓰도록
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
        if (tripUserId != commenter.id && findTrip.isPublic == true)
            notificationService.sendPushNotification(
                listOf(tripUserId),
                "새로운 여행의 시작, 트립스케치",
                "${commenter.nickname} 님이 댓글을 남겼습니다. ",
                nickname = commenter.nickname,
                profileUrl = commenter.profileImageUrl,
                tripId = comment.tripId,
                commentId = comment.id
            )
        return fromComment(createdComment, userService)
    }

    fun createChildrenComment(
        memberId: Long,
        parentId: String,
        commentChildrenCreateDto: CommentChildrenCreateDto
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

//        if (mentionedUser.email.endsWith("@delete.com")) {
//            throw BadRequestException("해당 언급 된 사용자는 없습니다.")
//        }

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
                commentId = childComment.id
            )
        }
        return fromComment(createdComment, userService)
    }

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
            updatedAt = updatedTime
        )

        val savedComment = commentRepository.save(updatedComment)

        return fromComment(savedComment, userService)
    }

    fun updateChildrenComment(
        memberId: Long,
        parentId: String,
        id: String,
        commentUpdateDto: CommentUpdateDto
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
            updatedAt = updatedTime
        )

        parentComment.children[childCommentIndex] = updatedChildComment
        val savedParentComment = commentRepository.save(parentComment)

        return fromComment(savedParentComment, userService)
    }

    fun deleteComment(memberId: Long, id: String) {
        val comment = commentRepository.findById(id).orElse(null)
            ?: throw IllegalArgumentException("해당 id 댓글은 존재하지 않습니다.")
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(comment.tripId)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")

        if (comment.isDeleted) {
            throw ForbiddenException("이미 삭제 된 댓글 입니다.")
        }
        val commenter =
            userRepository.findByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일의 사용자 존재하지 않습니다.")

        val adminIdsStrings = EnvLoader.getProperty("ADMIN_IDS")?.split(",") ?: listOf()
        val adminIds = adminIdsStrings.mapNotNull { it.toLongOrNull() }

        if (memberId in adminIds) {
            // Soft delete 처리 부분으로 넘어감
            val deletedComment = comment.copy(
                content = "삭제 된 댓글입니다.",
                isDeleted = true,
                likedBy = mutableSetOf(),
                numberOfLikes = 0
            )
            commentRepository.save(deletedComment)
            return
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
            numberOfLikes = 0
        )
        commentRepository.save(deletedComment)
    }

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
                    commentId = comment.id
                )
            }
        }

        val savedComment = commentRepository.save(comment)
        return fromComment(savedComment, userService)
    }

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
                    commentId = childComment.id
                )
            }
        }

        val savedParentComment = commentRepository.save(parentComment)
        return fromComment(savedParentComment, userService)
    }

    companion object {
        fun fromComment(comment: Comment, userService: UserService): CommentDto {

            val commenter = comment.userId?.let { userService.findUserById(it) }

            val commenterProfile = commenter?.let {
                UserProfileDto(
                    nickname = it.nickname,
                    introduction = it.introduction ?: "",
                    profileImageUrl = it.profileImageUrl ?: ""
                )
            }

            val mentionedUser = comment.replyToUserId?.let { userService.findUserById(it) }

            val mentionedUserNickname = mentionedUser?.nickname

            return CommentDto(
                id = comment.id,
                userNickName = commenterProfile?.nickname ?: "알 수 없는 사용자", // 사용자가 없을 경우 대비
                userProfileUrl = commenterProfile?.profileImageUrl
                    ?: "https://objectstorage.ap-osaka-1.oraclecloud.com/p/_EncCFAsYOUIwlJqRN7blRAETL9_l-fpCH-D07N4qig261ob7VHU8VIgtZaP-Thz/n/ax6izwmsuv9c/b/image-tripsketch/o/default-02.png", // 사용자가 없을 경우 대비
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

fun paginateComments(comments: List<CommentDto>, page: Int, pageSize: Int): Map<String, Any> {
    val paginatedComments = mutableListOf<CommentDto>()
    val totalCommentsWithChildren = comments.sumOf { it.children.size + 1 }
    val startIndex = (page - 1) * pageSize
    var addedComments = 0
    var totalIndex = 0
    var currentIndex = 0

    while (startIndex < pageSize && totalIndex < totalCommentsWithChildren) {
        val currentComment = comments[currentIndex]
        paginatedComments.add(currentComment)
        addedComments++
        var toAddChildIndex = 0


        // 대댓글 추가
        currentComment.children.forEachIndexed { _, childComment ->
            if (addedComments < pageSize) {
                paginatedComments[currentIndex].children.add(childComment)
                addedComments++
                toAddChildIndex++
            }
        }
        totalIndex=currentIndex+toAddChildIndex

        currentIndex++
    }

    val totalPage = (totalCommentsWithChildren + pageSize - 1) / pageSize

    return mapOf(
        "comments" to paginatedComments,
        "currentPage" to page,
        "totalPage" to totalPage,
        "commentsPerPage" to pageSize
    )
}


//fun paginateComments(comments: List<CommentDto>, page: Int, pageSize: Int): Map<String, Any> {
//    val totalCommentsWithChildren = comments.sumOf { it.children.size + 1 }
//    val startIndex = (page - 1) * pageSize
//    val endIndex = minOf(startIndex + pageSize, totalCommentsWithChildren)
//
//    val paginatedComments = mutableListOf<CommentDto>()
//
//    var currentIndex = startIndex
//    var addedComments = 0
//
//
//
//    val totalPage = (totalCommentsWithChildren + pageSize - 1) / pageSize
//
//    return mapOf(
//        "comments" to paginatedComments,
//        "currentPage" to page,
//        "totalPage" to totalPage,
//        "commentsPerPage" to pageSize
//    )
//}




