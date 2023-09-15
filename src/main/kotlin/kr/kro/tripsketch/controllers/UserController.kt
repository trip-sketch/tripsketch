package kr.kro.tripsketch.controllers

import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.dto.UserDto
import kr.kro.tripsketch.dto.UserUpdateDto
import kr.kro.tripsketch.exceptions.BadRequestException
import kr.kro.tripsketch.exceptions.DataNotFoundException
import kr.kro.tripsketch.exceptions.InternalServerException
import kr.kro.tripsketch.exceptions.UnauthorizedException
import kr.kro.tripsketch.services.ImageService
import kr.kro.tripsketch.services.KakaoOAuthService
import kr.kro.tripsketch.services.S3Service
import kr.kro.tripsketch.services.UserService
import kr.kro.tripsketch.utils.EnvLoader
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.services.s3.model.S3Exception

@RestController
@RequestMapping("api/user")
class UserController(
    private val userService: UserService,
    private val kakaoOAuthService: KakaoOAuthService,
    private val imageService: ImageService,
    private val s3Service: S3Service,
) {

    @GetMapping
    @ApiResponse(responseCode = "200", description = "사용자 정보를 성공적으로 반환합니다.")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 또는 유효하지 않은 토큰.")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    fun getUser(
        req: HttpServletRequest,
        @RequestParam token: String,
    ): ResponseEntity<Any> {
        val memberId = req.getAttribute("memberId") as Long
        val user = userService.findUserByMemberId(memberId)

        userService.storeUserPushToken(memberId, token)

        val adminIdsStrings = EnvLoader.getProperty("ADMIN_IDS")?.split(",") ?: listOf()
        val adminIds = adminIdsStrings.mapNotNull { it.toLongOrNull() }

        val isAdmin = memberId in adminIds

        return if (user != null) {
            ResponseEntity.ok(userService.toDto(user, true, isAdmin))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/nickname")
    @ApiResponse(responseCode = "200", description = "사용자의 닉네임으로 정보를 성공적으로 반환합니다.")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    fun getUserByNickname(@RequestParam nickname: String): ResponseEntity<UserDto> {
        val user = userService.findUserByNickname(nickname)
        return if (user != null) {
            ResponseEntity.ok(userService.toDto(user, false))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping(consumes = ["multipart/form-data"])
    @ApiResponse(responseCode = "200", description = "사용자 정보 업데이트가 성공적으로 완료되었습니다.")
    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    @ApiResponse(responseCode = "401", description = "이메일이 존재하지 않습니다.")
    fun updateUser(
        req: HttpServletRequest,
        @Validated @ModelAttribute userUpdateDto: UserUpdateDto,
    ): ResponseEntity<UserDto> {
        val memberId = req.getAttribute("memberId") as Long? ?: throw UnauthorizedException("해당 사용자가 존재하지 않습니다.")

        try {
            val updatedUser = userService.updateUser(memberId, userUpdateDto)
            return ResponseEntity.ok(userService.toDto(updatedUser))
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("요청이 잘못되었습니다: ${e.message}")
        }
    }

    @GetMapping("/admin/users")
    @ApiResponse(responseCode = "200", description = "모든 사용자의 정보를 성공적으로 반환합니다.")
    fun getAllUsers(req: HttpServletRequest, pageable: Pageable): ResponseEntity<Page<UserDto>> {
        val users = userService.getAllUsers(pageable)
        return ResponseEntity.ok(users.map { userService.toDto(it) })
    }

    @DeleteMapping("/unlink")
    @ApiResponse(responseCode = "200", description = "카카오 연동 해제 및 회원 탈퇴 처리가 성공적으로 이루어졌습니다.")
    @ApiResponse(responseCode = "401", description = "해당 사용자가 존재하지 않습니다.")
    @ApiResponse(responseCode = "404", description = "회원 정보를 찾을 수 없습니다.")
    @ApiResponse(responseCode = "400", description = "카카오 Refresh Token이 없습니다.")
    @ApiResponse(responseCode = "500", description = "카카오 연동 해제에 실패했습니다.")
    fun unlinkKakaoUser(req: HttpServletRequest): ResponseEntity<Any> {
        val memberId = req.getAttribute("memberId") as Long?
            ?: throw UnauthorizedException("해당 사용자가 존재하지 않습니다.")

        val user = userService.findUserByMemberId(memberId)
            ?: throw DataNotFoundException("회원 정보를 찾을 수 없습니다.")

        val accessToken = kakaoOAuthService.refreshAccessToken(user.kakaoRefreshToken!!)
            ?: throw BadRequestException("카카오 Refresh Token이 없습니다.")

        if (!kakaoOAuthService.unlinkUser(accessToken)) {
            throw InternalServerException("카카오 연동 해제에 실패했습니다")
        }

        userService.softDeleteUserByMemberId(memberId)
        return ResponseEntity.ok(mapOf("message" to "회원 탈퇴가 성공적으로 처리되었습니다."))
    }

    @PostMapping("/upload", consumes = ["multipart/form-data"])
    fun uploadFile(
        @RequestParam("dir", required = false, defaultValue = "") dir: String,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<Any> {
        return try {
            val (url, response) = s3Service.uploadFile(dir, file)
            ResponseEntity.ok(mapOf("url" to url, "eTag" to response.eTag()))
        } catch (e: S3Exception) {
            ResponseEntity.badRequest().body(mapOf("message" to "파일 업로드 실패", "awsError" to e.message))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("message" to "알 수 없는 오류 발생", "error" to e.message))
        }
    }

    @PostMapping("/uploads", consumes = ["multipart/form-data"])
    fun uploadFiles(
        @RequestParam("dir", required = false, defaultValue = "") dir: String?,
        @RequestParam("files") files: Array<MultipartFile>,
    ): ResponseEntity<Any> {
        val directory = dir ?: ""

        return try {
            val results = files.map { file ->
                val (url, response) = s3Service.uploadFile(directory, file)
                mapOf("url" to url, "eTag" to response.eTag())
            }
            ResponseEntity.ok(results)
        } catch (e: S3Exception) {
            ResponseEntity.badRequest().body(mapOf("message" to "파일 업로드 실패", "awsError" to e.message))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("message" to "알 수 없는 오류 발생", "error" to e.message))
        }
    }

    @DeleteMapping("/delete")
    fun deleteImage(@RequestParam url: String): ResponseEntity<String> {
        return try {
            imageService.deleteImage(url)
            ResponseEntity.ok("Image successfully deleted!")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Failed to delete the image. Error: ${e.message}")
        }
    }
}
