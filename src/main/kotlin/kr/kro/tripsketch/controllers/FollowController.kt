package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.domain.Follow
import kr.kro.tripsketch.services.FollowService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/follows")
class FollowController(private val followService: FollowService) {

    @PostMapping
    fun follow(@RequestParam follower: String, @RequestParam following: String) {
        followService.follow(follower, following)
    }

    @DeleteMapping
    fun unfollow(@RequestParam follower: String, @RequestParam following: String) {
        followService.unfollow(follower, following)
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
