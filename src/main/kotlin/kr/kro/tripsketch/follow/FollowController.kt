package kr.kro.tripsketch.follow

import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.dto.FollowDto
import kr.kro.tripsketch.dto.ProfileDto
import kr.kro.tripsketch.exceptions.UnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/**
 * 구독(Follow) 기능을 처리하는 컨트롤러
 *
 * @author Hojun Song
 */
@Validated
@RestController
@RequestMapping("api/follow")
class FollowController(
    private val followService: FollowService,
) {

    /**
     * 사용자를 구독하는 메서드입니다.
     */
    @PostMapping
    @ApiResponse(responseCode = "200", description = "성공적으로 구독했습니다.")
    @ApiResponse(responseCode = "401", description = "이메일이 존재하지 않습니다.")
    fun follow(req: HttpServletRequest, @Validated @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val memberId = req.getAttribute("memberId") as Long?
            ?: throw UnauthorizedException("해당 사용자가 존재하지 않습니다.")
        followService.follow(memberId, followDto.nickname)
        return ResponseEntity.status(HttpStatus.OK).body("구독에 성공했습니다.")
    }

    /**
     * 사용자의 구독을 취소하는 메서드입니다.
     */
    @DeleteMapping
    @ApiResponse(responseCode = "200", description = "구독 취소되었습니다.")
    @ApiResponse(responseCode = "401", description = "이메일이 존재하지 않습니다.")
    fun unfollow(req: HttpServletRequest, @Validated @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val memberId = req.getAttribute("memberId") as Long?
            ?: throw UnauthorizedException("해당 사용자가 존재하지 않습니다.")
        followService.unfollow(memberId, followDto.nickname)
        return ResponseEntity.status(HttpStatus.OK).body("구독 취소되었습니다.")
    }

    /**
     * 나를 팔로우 한 사용자의 나에 대한 구독을 취소하는 메서드입니다.
     */
    @DeleteMapping("/me")
    @ApiResponse(responseCode = "200", description = "해당 사용자의 구독을 취소했습니다.")
    @ApiResponse(responseCode = "401", description = "이메일이 존재하지 않습니다.")
    fun unfollowMe(req: HttpServletRequest, @Validated @RequestBody followDto: FollowDto): ResponseEntity<String> {
        val memberId = req.getAttribute("memberId") as Long?
            ?: throw UnauthorizedException("해당 사용자가 존재하지 않습니다.")
        followService.unfollowMe(memberId, followDto.nickname)
        return ResponseEntity.status(HttpStatus.OK).body("해당 사용자의 구독을 취소했습니다.")
    }

    /**
     * 사용자의 구독 리스트를 반환하는 메서드입니다.
     */
    @GetMapping("/user/followings")
    @ApiResponse(responseCode = "200", description = "사용자의 구독 리스트를 반환합니다.")
    fun getFollowings(req: HttpServletRequest, @RequestParam nickname: String): List<ProfileDto> {
        val currentUserMemberId = req.getAttribute("memberId") as Long?
        return followService.getFollowings(nickname, currentUserMemberId)
    }

    /**
     * 사용자를 구독하는 사람의 리스트를 반환하는 메서드입니다.
     */
    @GetMapping("/user/followers")
    @ApiResponse(responseCode = "200", description = "사용자를 구독하는 사람의 리스트를 반환합니다.")
    fun getFollowers(req: HttpServletRequest, @RequestParam nickname: String): List<ProfileDto> {
        val currentUserMemberId = req.getAttribute("memberId") as Long?
        return followService.getFollowers(nickname, currentUserMemberId)
    }

    /**
     * 비회원 사용자가 특정 사용자의 구독리스트를 반환하는 메서드입니다.
     */
    @GetMapping("/guest/followings")
    @ApiResponse(responseCode = "200", description = "사용자의 구독 리스트를 반환합니다.")
    fun getFollowingsByGuest(@RequestParam nickname: String): List<ProfileDto> {
        return followService.getFollowings(nickname, null)
    }

    /**
     *  비회원 사용자가 특정 사용자를 구독하는 리스트를 반환하는 메서드입니다.
     */
    @GetMapping("/guest/followers")
    @ApiResponse(responseCode = "200", description = "사용자를 구독하는 사람의 리스트를 반환합니다.")
    fun getFollowersByGuest(@RequestParam nickname: String): List<ProfileDto> {
        return followService.getFollowers(nickname, null)
    }
}
