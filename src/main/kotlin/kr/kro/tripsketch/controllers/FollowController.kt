package kr.kro.tripsketch.controllers

import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import kr.kro.tripsketch.dto.FollowDto
import kr.kro.tripsketch.dto.ProfileDto
import kr.kro.tripsketch.services.FollowService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.exceptions.UnauthorizedException

@Validated
@RestController
@RequestMapping("api/follow")
class FollowController(
    private val followService: FollowService,
) {

    @PostMapping
    @ApiResponse(responseCode = "200", description = "성공적으로 구독했습니다.")
    @ApiResponse(responseCode = "401", description = "이메일이 존재하지 않습니다.")
    fun follow(req: HttpServletRequest, @Valid @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val email = req.getAttribute("userEmail") as String?
            ?: throw UnauthorizedException("이메일이 존재하지 않습니다.")
        followService.follow(email, followDto.nickname)
        return ResponseEntity.status(HttpStatus.OK).body("성공적으로 구독했습니다.")
    }

    @DeleteMapping
    @ApiResponse(responseCode = "200", description = "구독 취소되었습니다.")
    @ApiResponse(responseCode = "401", description = "이메일이 존재하지 않습니다.")
    fun unfollow(req: HttpServletRequest, @Valid @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val email = req.getAttribute("userEmail") as String?
            ?: throw UnauthorizedException("이메일이 존재하지 않습니다.")
        followService.unfollow(email, followDto.nickname)
        return ResponseEntity.status(HttpStatus.OK).body("구독 취소되었습니다.")
    }

    @DeleteMapping("/unfollowMe")
    @ApiResponse(responseCode = "200", description = "해당 사용자의 구독을 취소했습니다.")
    @ApiResponse(responseCode = "401", description = "이메일이 존재하지 않습니다.")
    fun unfollowMe(req: HttpServletRequest, @Valid @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val email = req.getAttribute("userEmail") as String?
            ?: throw UnauthorizedException("이메일이 존재하지 않습니다.")
        followService.unfollowMe(email, followDto.nickname)
        return ResponseEntity.status(HttpStatus.OK).body("해당 사용자의 구독을 취소했습니다.")
    }

    @GetMapping("/followings")
    @ApiResponse(responseCode = "200", description = "사용자의 구독 리스트를 반환합니다.")
    fun getFollowings(@RequestParam nickname: String): List<ProfileDto> {
        return followService.getFollowings(nickname)
    }

    @GetMapping("/followers")
    @ApiResponse(responseCode = "200", description = "사용자를 구독하는 사람의 리스트를 반환합니다.")
    fun getFollowers(@RequestParam nickname: String): List<ProfileDto> {
        return followService.getFollowers(nickname)
    }
}
