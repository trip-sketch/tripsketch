package kr.kro.tripsketch.controllers

import jakarta.validation.Valid
import kr.kro.tripsketch.annotations.TokenValidation
import kr.kro.tripsketch.dto.FollowDto
import kr.kro.tripsketch.dto.ProfileDto
import kr.kro.tripsketch.services.FollowService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletRequest

@Validated
@RestController
@RequestMapping("api/follow")
class FollowController(
    private val followService: FollowService,
) {

    @TokenValidation
    @PostMapping
    fun follow(req: HttpServletRequest, @Valid @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val email = req.getAttribute("userEmail") as String?
            ?: throw IllegalArgumentException("이메일이 존재하지 않습니다.")
        followService.follow(email, followDto.nickname)
        return ResponseEntity.status(HttpStatus.OK).body("성공적으로 구독했습니다.")
    }

    @TokenValidation
    @DeleteMapping
    fun unfollow(req: HttpServletRequest, @Valid @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val email = req.getAttribute("userEmail") as String?
            ?: throw IllegalArgumentException("이메일이 존재하지 않습니다.")
        followService.unfollow(email, followDto.nickname)
        return ResponseEntity.status(HttpStatus.OK).body("구독 취소되었습니다.")
    }

    @TokenValidation
    @DeleteMapping("/unfollowMe")
    fun unfollowMe(req: HttpServletRequest, @Valid @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val email = req.getAttribute("userEmail") as String?
            ?: throw IllegalArgumentException("이메일이 존재하지 않습니다.")
        followService.unfollowMe(email, followDto.nickname)
        return ResponseEntity.status(HttpStatus.OK).body("해당 사용자의 구독을 취소했습니다.")
    }

    @GetMapping("/followings")
    fun getFollowings(@RequestParam nickname: String): List<ProfileDto> {
        return followService.getFollowings(nickname)
    }

    @GetMapping("/followers")
    fun getFollowers(@RequestParam nickname: String): List<ProfileDto> {
        return followService.getFollowers(nickname)
    }
}
