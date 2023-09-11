package kr.kro.tripsketch.controllers

import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.dto.UserDto
import kr.kro.tripsketch.dto.UserUpdateDto
import kr.kro.tripsketch.exceptions.BadRequestException
import kr.kro.tripsketch.exceptions.UnauthorizedException
import kr.kro.tripsketch.services.ImageService
import kr.kro.tripsketch.services.NotificationService
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
class UserController(private val userService: UserService, private val imageService: ImageService, private val s3Service: S3Service) {

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

        // 관리자 이메일 리스트를 환경 변수에서 가져오기
        val adminIdsStrings = EnvLoader.getProperty("ADMIN_IDS")?.split(",") ?: listOf()
        val adminIds = adminIdsStrings.mapNotNull { it.toLongOrNull() }

// 사용자 ID가 관리자 ID 리스트에 있는지 확인
        val isAdmin = memberId in adminIds

        return if (user != null) {
            ResponseEntity.ok(userService.toDto(user, true, isAdmin)) // 관리자 여부 추가
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
            ResponseEntity.ok(userService.toDto(user, false)) // 이메일 미포함, 관리자 여부 미포함
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
