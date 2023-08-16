package kr.kro.tripsketch.controllers

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import kr.kro.tripsketch.dto.FollowDto
import kr.kro.tripsketch.dto.ProfileDto
import kr.kro.tripsketch.dto.UserProfileDto
import kr.kro.tripsketch.services.FollowService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*


@Validated
@RestController
@RequestMapping("api/follow")
class FollowController(private val followService: FollowService) {

    @PostMapping
    fun follow(@RequestHeader("Authorization") token: String, @Valid @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val actualToken = token.removePrefix("Bearer ").trim()
        followService.follow(actualToken, followDto.nickname)
        return ResponseEntity.status(HttpStatus.OK).body("성공적으로 구독했습니다.")
    }

    @DeleteMapping
    fun unfollow(@RequestHeader("Authorization") token: String, @Valid @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val actualToken = token.removePrefix("Bearer ").trim()
        followService.unfollow(actualToken, followDto.nickname)
        return ResponseEntity.status(HttpStatus.OK).body("구독 취소되었습니다. ")
    }

    @DeleteMapping("/unfollowMe")
    fun unfollowMe(@RequestHeader("Authorization") token: String, @Valid @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val actualToken = token.removePrefix("Bearer ").trim()
        followService.unfollowMe(actualToken, followDto.nickname)
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