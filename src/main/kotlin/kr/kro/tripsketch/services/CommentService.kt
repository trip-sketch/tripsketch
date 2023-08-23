package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Comment
import kr.kro.tripsketch.dto.CommentDto
import kr.kro.tripsketch.dto.CommentUpdateDto
import kr.kro.tripsketch.dto.CommentCreateDto
import kr.kro.tripsketch.dto.UserProfileDto
import kr.kro.tripsketch.exceptions.ForbiddenException
import kr.kro.tripsketch.repositories.CommentRepository
import kr.kro.tripsketch.repositories.UserRepository
import org.bson.types.ObjectId // ObjectId를 사용하기 위한 import
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) {

    fun getAllComments(pageable: Pageable): Page<CommentDto> {
        return commentRepository.findAll(pageable).map { fromComment(it, userRepository) }
    }

    fun getCommentByTripId(tripId: String): List<CommentDto> {
        val comments = commentRepository.findAllByTripId(tripId)
        return comments.map { fromComment(it, userRepository) }
    }

    /** 로그인 한 유저가 좋아요가 있는 댓글 조회 */
    fun getIsLikedByTokenForTrip(email: String, tripId: String): List<CommentDto> {
        val updatedComments = isLikedByTokenForComments(email, tripId)

        return updatedComments.map { fromComment(it, userRepository) }
    }


    private fun isLikedByTokenForComments(userEmail: String, tripId: String): List<Comment> {
        val comments = commentRepository.findAllByTripId(tripId)

        return comments.map { comment ->
            val isLiked = comment.likedBy.contains(userEmail)
            val updatedComment = comment.copy(isLiked = isLiked)

            val updatedChildren = comment.children.map { child ->
                child.copy(isLiked = child.likedBy.contains(userEmail))
            }.toMutableList()

            updatedComment.copy(children = updatedChildren)
        }
    }

    fun createComment(email: String, commentCreateDto: CommentCreateDto): CommentDto {

        val parentComment: Comment? = commentCreateDto.parentId?.let {
            commentRepository.findById(it).orElse(null)
        }

        val comment = Comment(
            userEmail = email,
            tripId = commentCreateDto.tripId,
            parentId = commentCreateDto.parentId,
            content = commentCreateDto.content,
            replyTo = commentCreateDto.replyTo,
        )

        if (parentComment == null) {
            // parentId가 없는 경우: 새로운 댓글을 저장하고 반환
            val createdComment = commentRepository.save(comment)
            return fromComment(createdComment, userRepository)
        } else {
            // parentId가 있는 경우: 새로운 댓글을 부모의 children 리스트에 추가하고 부모 댓글을 저장
            val childComment = Comment(
                id = ObjectId().toString(), // 새로운 ObjectId 생성
                userEmail = email,
                tripId = commentCreateDto.tripId,
                parentId = commentCreateDto.parentId,
                content = commentCreateDto.content,
                replyTo = commentCreateDto.replyTo,
            )
            parentComment.children.add(childComment)
            val createdComment = commentRepository.save(parentComment)
            return fromComment(createdComment, userRepository)
        }
    }

    fun updateComment(email: String, id: String, commentUpdateDto: CommentUpdateDto): CommentDto {
        val comment =
            commentRepository.findById(id).orElse(null) ?: throw IllegalArgumentException("해당 id 댓글은 존재하지 않습니다.")
        if(comment.userEmail != email){
            throw ForbiddenException("해당 사용자만 접근 가능합니다.")
        }
        val updatedTime = LocalDateTime.now()
        val updatedComment = comment.copy(
            content = commentUpdateDto.content ?: comment.content,
            updatedAt = updatedTime
        )

        val savedComment = commentRepository.save(updatedComment)

        return fromComment(savedComment, userRepository)
    }

    fun updateChildrenComment(email: String, parentId: String, id: String, commentUpdateDto: CommentUpdateDto): CommentDto {
        val parentComment = commentRepository.findById(parentId).orElse(null)
            ?: throw IllegalArgumentException("해당 parentId 댓글은 존재하지 않습니다.")

        val childCommentIndex = parentComment.children.indexOfFirst { it.id == id }
        if (childCommentIndex == -1) {
            throw IllegalArgumentException("해당 id에 대응하는 댓글이 children 존재하지 않습니다.")
        }

        if(parentComment.children[childCommentIndex].userEmail != email){
            throw ForbiddenException("해당 사용자만 접근 가능합니다.")
        }

        val updatedTime = LocalDateTime.now()
        val updatedChildComment = parentComment.children[childCommentIndex].copy(
            content = commentUpdateDto.content ?: parentComment.children[childCommentIndex].content,
            updatedAt = updatedTime
        )

        parentComment.children[childCommentIndex] = updatedChildComment
        val savedParentComment = commentRepository.save(parentComment)

        return fromComment(savedParentComment, userRepository)
    }

    fun deleteComment(email: String, id: String) {
        val comment = commentRepository.findById(id).orElse(null)
            ?: throw IllegalArgumentException("해당 id 댓글은 존재하지 않습니다.")
        if(comment.userEmail != email){
            throw ForbiddenException("해당 사용자만 접근 가능합니다.")
        }
        // Soft delete 처리
        val deletedComment = comment.copy(isDeleted = true)
        commentRepository.save(deletedComment)
    }

    fun deleteChildrenComment(email: String, parentId: String, id: String) {
        val parentComment = commentRepository.findById(parentId).orElse(null)
            ?: throw IllegalArgumentException("해당 parentId 댓글은 존재하지 않습니다.")

        val childCommentIndex = parentComment.children.indexOfFirst { it.id == id }
        if (childCommentIndex == -1) {
            throw IllegalArgumentException("해당 id에 대응하는 댓글이 children에 존재하지 않습니다.")
        }

        if(parentComment.children[childCommentIndex].userEmail != email){
            throw ForbiddenException("해당 사용자만 접근 가능합니다.")
        }

        // Soft delete 처리
        val deletedChildComment = parentComment.children[childCommentIndex].copy(isDeleted = true)
        parentComment.children[childCommentIndex] = deletedChildComment
        commentRepository.save(parentComment)
    }

    fun toggleLikeComment(email: String, id: String): CommentDto {
        val comment = commentRepository.findById(id).orElse(null)
            ?: throw IllegalArgumentException("해당 id 댓글은 존재하지 않습니다.")
        if (comment.likedBy.contains(email)) {
            comment.likedBy.remove(email) // 이미 좋아요를 누른 경우 좋아요 취소
            comment.numberOfLikes -= 1
        } else {
            comment.likedBy.add(email) // 좋아요 추가
            comment.numberOfLikes += 1
        }

        val savedComment = commentRepository.save(comment)
        return fromComment(savedComment, userRepository)
    }

    fun toggleLikeChildrenComment(email: String, parentId: String, id: String): CommentDto {
        val parentComment = commentRepository.findById(parentId).orElse(null)
            ?: throw IllegalArgumentException("해당 parentId 댓글은 존재하지 않습니다.")

        val childCommentIndex = parentComment.children.indexOfFirst { it.id == id }
        if (childCommentIndex == -1) {
            throw IllegalArgumentException("해당 id에 대응하는 댓글이 children에 존재하지 않습니다.")
        }

        val childComment = parentComment.children[childCommentIndex]
        if (childComment.likedBy.contains(email)) {
            childComment.likedBy.remove(email) // 이미 좋아요를 누른 경우 좋아요 취소
            childComment.numberOfLikes -= 1
        } else {
            childComment.likedBy.add(email) // 좋아요 추가
            childComment.numberOfLikes += 1
        }

        val savedParentComment = commentRepository.save(parentComment)
        return fromComment(savedParentComment, userRepository)
    }

    companion object {
        fun fromComment(comment: Comment, userRepository: UserRepository): CommentDto {
            val commenter = userRepository.findByEmail(comment.userEmail)
            val commenterProfile = commenter?.let {
                UserProfileDto(
                    email = it.email,
                    nickname = it.nickname,
                    introduction = it.introduction,
                    profileImageUrl = it.profileImageUrl
                )
            }

            return CommentDto(
                id = comment.id,
                userEmail = comment.userEmail,
                userNickName = commenterProfile?.nickname ?: "", // 사용자가 없을 경우 대비
                userProfileUrl = commenterProfile?.profileImageUrl ?: "", // 사용자가 없을 경우 대비
                tripId = comment.tripId,
                parentId = comment.parentId,
                content = comment.content,
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt,
                replyTo = comment.replyTo,
                isDeleted = comment.isDeleted,
                isLiked = comment.isLiked,
                numberOfLikes = comment.numberOfLikes,
                children = comment.children.map { fromComment(it, userRepository) }.toMutableList(),
            )
        }
    }
}
