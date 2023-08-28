package kr.kro.tripsketch.controllers

import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.dto.TripCreateDto
import kr.kro.tripsketch.dto.TripUpdateDto
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.dto.TripIdAndEmailDto
import kr.kro.tripsketch.services.TripService
import kr.kro.tripsketch.services.TripLikeService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kr.kro.tripsketch.services.JwtService


@RestController
@RequestMapping("api/trip")
class TripLikeController(private val tripLikeService: TripLikeService) {

    @PostMapping("/like")
    fun likeTrip(
        req: HttpServletRequest,
//        @PathVariable id: String
        @RequestBody tripIdAndEmailDto: TripIdAndEmailDto
    ): ResponseEntity<String> {
        val email = req.getAttribute("userEmail") as String
        return try {
//            tripLikeService.likeTrip(email, id)
            tripLikeService.likeTrip(tripIdAndEmailDto)
            ResponseEntity.ok("게시물을 좋아요 하였습니다.")
        } catch (ex: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
//        catch (ex: IllegalStateException) {
//            ResponseEntity.badRequest().body(ex.message)
//        }
    }

    @PostMapping("/unlike")
    fun unlikeTrip(
        req: HttpServletRequest,
//        @PathVariable id: String
        @RequestBody tripIdAndEmailDto: TripIdAndEmailDto
    ): ResponseEntity<String> {
        val email = req.getAttribute("userEmail") as String
        return try {
//            tripLikeService.unlikeTrip(email, id)
            tripLikeService.likeTrip(tripIdAndEmailDto)
            ResponseEntity.ok("게시물 좋아요를 취소하였습니다.")
        } catch (ex: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
//        catch (ex: IllegalStateException) {
//            ResponseEntity.badRequest().body(ex.message)
//        }
    }
}


class EntityNotFoundException(message: String) : RuntimeException(message)
