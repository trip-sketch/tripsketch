package kr.kro.tripsketch.controllers

import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.dto.TripCreateDto
import kr.kro.tripsketch.dto.TripUpdateDto
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.services.TripService
import kr.kro.tripsketch.services.TripLikeService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kr.kro.tripsketch.services.JwtService


@RequestMapping("api/trip/like")
class TripLikeController(private val tripLikeService: TripLikeService) {

    @PostMapping("/{id}/like")
    fun likeTrip(
        req: HttpServletRequest,
        @PathVariable id: String
    ): ResponseEntity<String> {
        val email = req.getAttribute("userEmail") as String
        try {
            tripLikeService.likeTrip(email, id)
            return ResponseEntity.ok("게시물이 좋아요 되었습니다.")
        } catch (ex: EntityNotFoundException) {
            return ResponseEntity.notFound().build()
        } catch (ex: IllegalStateException) {
            return ResponseEntity.badRequest().body(ex.message)
        }
    }

    @PostMapping("/{id}/unlike")
    fun unlikeTrip(
        req: HttpServletRequest,
        @PathVariable id: String
    ): ResponseEntity<String> {
        val email = req.getAttribute("userEmail") as String
        try {
            tripLikeService.unlikeTrip(email, id)
            return ResponseEntity.ok("게시물 좋아요가 취소되었습니다.")
        } catch (ex: EntityNotFoundException) {
            return ResponseEntity.notFound().build()
        } catch (ex: IllegalStateException) {
            return ResponseEntity.badRequest().body(ex.message)
        }
    }
}


class EntityNotFoundException(message: String) : RuntimeException(message)
