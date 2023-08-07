package kr.kro.tripsketch.controllers

import jakarta.validation.Valid
import kr.kro.tripsketch.domain.Follow
import kr.kro.tripsketch.dto.FollowDto
import kr.kro.tripsketch.services.FollowService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/follows")
class FollowController(private val followService: FollowService) {

    @PostMapping
    fun follow(@RequestHeader("Authorization") token: String, @Valid @RequestBody followDto: FollowDto) {
        val actualToken = token.removePrefix("Bearer ").trim()
        followService.follow(actualToken, followDto.email)
    }

    @DeleteMapping
    fun unfollow(@RequestHeader("Authorization") token: String, @Valid @RequestBody followDto: FollowDto) {
        val actualToken = token.removePrefix("Bearer ").trim()
        followService.unfollow(actualToken, followDto.email)
    }

    @DeleteMapping("/unfollowMe")
    fun unfollowMe(@RequestHeader("Authorization") token: String, @Valid @RequestBody followDto: FollowDto) {
        val actualToken = token.removePrefix("Bearer ").trim()
        followService.unfollowMe(actualToken, followDto.email)
    }

    @GetMapping("/followings")
    fun getFollowings(@RequestParam follower: String): List<Follow> {
        return followService.getFollowings(follower)
    }

    @GetMapping("/followers")
    fun getFollowers(@RequestParam following: String): List<Follow> {
        return followService.getFollowers(following)
    }
}