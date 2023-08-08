package kr.kro.tripsketch.controllers

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import kr.kro.tripsketch.dto.FollowDto
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
        followService.follow(actualToken, followDto.email)
        return ResponseEntity.status(HttpStatus.OK).body("성공적으로 구독했습니다.")
    }

    @DeleteMapping
    fun unfollow(@RequestHeader("Authorization") token: String, @Valid @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val actualToken = token.removePrefix("Bearer ").trim()
        followService.unfollow(actualToken, followDto.email)
        return ResponseEntity.status(HttpStatus.OK).body("구독 취소되었습니다. ")
    }

    @DeleteMapping("/unfollowMe")
    fun unfollowMe(@RequestHeader("Authorization") token: String, @Valid @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val actualToken = token.removePrefix("Bearer ").trim()
        followService.unfollowMe(actualToken, followDto.email)
        return ResponseEntity.status(HttpStatus.OK).body("해당 사용자의 구독을 취소했습니다.")
    }

    @GetMapping("/followings")
    fun getFollowings(@Email @RequestParam follower: String): List<UserProfileDto> {
        return followService.getFollowings(follower)
    }

    @GetMapping("/followers")
    fun getFollowers(@Email @RequestParam following: String): List<UserProfileDto> {
        return followService.getFollowers(following)
    }
}