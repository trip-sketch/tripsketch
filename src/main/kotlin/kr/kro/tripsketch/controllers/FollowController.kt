package kr.kro.tripsketch.controllers

import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.dto.FollowDto
import kr.kro.tripsketch.dto.ProfileDto
import kr.kro.tripsketch.exceptions.UnauthorizedException
import kr.kro.tripsketch.services.FollowService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("api/follow")
class FollowController(
    private val followService: FollowService,
) {

    @PostMapping
    @ApiResponse(responseCode = "200", description = "성공적으로 구독했습니다.")
    @ApiResponse(responseCode = "401", description = "이메일이 존재하지 않습니다.")
    fun follow(req: HttpServletRequest, @Validated @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val memberId = req.getAttribute("memberId") as Long?
            ?: throw UnauthorizedException("해당 사용자가 존재하지 않습니다.")
        val responseFromExpo = followService.follow(memberId, followDto.nickname)
        return ResponseEntity.status(HttpStatus.OK).body(responseFromExpo)
    }

    @DeleteMapping
    @ApiResponse(responseCode = "200", description = "구독 취소되었습니다.")
    @ApiResponse(responseCode = "401", description = "이메일이 존재하지 않습니다.")
    fun unfollow(req: HttpServletRequest, @Validated @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val memberId = req.getAttribute("memberId") as Long?
            ?: throw UnauthorizedException("해당 사용자가 존재하지 않습니다.")
        followService.unfollow(memberId, followDto.nickname)
        return ResponseEntity.status(HttpStatus.OK).body("구독 취소되었습니다.")
    }

    @DeleteMapping("/unfollowMe")
    @ApiResponse(responseCode = "200", description = "해당 사용자의 구독을 취소했습니다.")
    @ApiResponse(responseCode = "401", description = "이메일이 존재하지 않습니다.")
    fun unfollowMe(req: HttpServletRequest, @Validated @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val memberId = req.getAttribute("memberId") as Long?
            ?: throw UnauthorizedException("해당 사용자가 존재하지 않습니다.")
        followService.unfollowMe(memberId, followDto.nickname)
        return ResponseEntity.status(HttpStatus.OK).body("해당 사용자의 구독을 취소했습니다.")
    }

    @GetMapping("/user/followings")
    @ApiResponse(responseCode = "200", description = "사용자의 구독 리스트를 반환합니다.")
    fun getFollowings(req: HttpServletRequest, @RequestParam nickname: String): List<ProfileDto> {
        val currentUserMemberId = req.getAttribute("memberId") as Long?
        return followService.getFollowings(nickname, currentUserMemberId)
    }

    @GetMapping("/user/followers")
    @ApiResponse(responseCode = "200", description = "사용자를 구독하는 사람의 리스트를 반환합니다.")
    fun getFollowers(req: HttpServletRequest, @RequestParam nickname: String): List<ProfileDto> {
        val currentUserMemberId = req.getAttribute("memberId") as Long?
        return followService.getFollowers(nickname, currentUserMemberId)
    }

    @GetMapping("/guest/followings")
    @ApiResponse(responseCode = "200", description = "사용자의 구독 리스트를 반환합니다.")
    fun getFollowingsByGuest(@RequestParam nickname: String): List<ProfileDto> {
        return followService.getFollowings(nickname, null)
    }

    @GetMapping("/guest/followers")
    @ApiResponse(responseCode = "200", description = "사용자를 구독하는 사람의 리스트를 반환합니다.")
    fun getFollowersByGuest(@RequestParam nickname: String): List<ProfileDto> {
        return followService.getFollowers(nickname, null)
    }
}
