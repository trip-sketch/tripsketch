import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import kr.kro.tripsketch.dto.ProfileDto
import kr.kro.tripsketch.repositories.UserRepository
import kr.kro.tripsketch.services.JwtService
import kr.kro.tripsketch.services.OracleObjectStorageService
import kr.kro.tripsketch.services.UserService

@Service
class ImageService(
    private val userRepository: UserRepository,
    private val oracleObjectStorageService: OracleObjectStorageService,
    private val userService: UserService,
    private val jwtService : JwtService
) {

    fun uploadProfileImage(token: String, file: MultipartFile): String {
        val userEmail: String? = jwtService.getEmailFromToken(token)
        if (userEmail == null || userRepository.findByEmail(userEmail) == null) {
            throw UserNotFoundException("사용자가 존재하지 않습니다.")
        }

        // 파일 업로드
        val bucketName = "tripsketch"
        val (uploadedImageUrl, error) = oracleObjectStorageService.uploadImageAndGetUrl(bucketName, file)

        if (error != null) {
            throw UploadFailedException(error)
        }

        val profileDto = ProfileDto(
            // 안에서 쓰는 변수는 소문자, 외부에서 가져다쓰는 변수는 대문자
            nickname = null,
            profileImageUrl = uploadedImageUrl,
            introduction = null
        )

        userService.updateUser(token, profileDto)
        return uploadedImageUrl!!
    }

    class UploadFailedException(message: String) : RuntimeException(message)

}
class UserNotFoundException(message: String) : RuntimeException(message)


