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


@RestController
@RequestMapping("api/trip/like")
class TripLikeController(private val tripLikeService: TripLikeService) {

    @PostMapping("/{id}/like")
    fun likeTrip(
        req: HttpServletRequest,
        @PathVariable id: String
    ): ResponseEntity<String> {
        val email = req.getAttribute("userEmail") as String
        return try {
            tripLikeService.likeTrip(email, id)
            ResponseEntity.ok("게시물을 좋아요 하였습니다.")
        } catch (ex: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
//        catch (ex: IllegalStateException) {
//            ResponseEntity.badRequest().body(ex.message)
//        }
    }

    @PostMapping("/{id}/unlike")
    fun unlikeTrip(
        req: HttpServletRequest,
        @PathVariable id: String
    ): ResponseEntity<String> {
        val email = req.getAttribute("userEmail") as String
        return try {
            tripLikeService.unlikeTrip(email, id)
            ResponseEntity.ok("게시물 좋아요가 취소되었습니다.")
        } catch (ex: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
//        catch (ex: IllegalStateException) {
//            ResponseEntity.badRequest().body(ex.message)
//        }
    }
}


class EntityNotFoundException(message: String) : RuntimeException(message)
