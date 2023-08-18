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
        val userProfile = userEmail?.let { userRepository.findByEmail(it) }
                ?: throw UserNotFoundException("사용자가 존재하지 않습니다.")

        // 파일 업로드
        val uploadedImageUrl = oracleObjectStorageService.uploadImageAndGetUrl(file)
        val ProfileDto = ProfileDto(
                // 안에서 쓰는 변수는 소문자, 외부에서 가져다쓰는 변수는 대문자
                nickname = null,
                profileImageUrl = uploadedImageUrl,
                introduction = null
        )

        userService.updateUser(token, ProfileDto)
        return uploadedImageUrl
    }

//    fun getDownloadImageUrl(token: String): String {
//        val userEmail: String? = jwtService.getEmailFromToken(token)
//        val userProfile = userEmail?.let { userRepository.findByEmail(it) }
//                ?: throw UserNotFoundException("사용자가 존재하지 않습니다.")
//
//        val profileImageUrl = userProfile.profileImageUrl
//        val downloadImageUrl = oracleObjectStorageService.downloadImageAndGetUrl(profileImageUrl)
//
//        return downloadImageUrl
//    }
}
class UserNotFoundException(message: String) : RuntimeException(message)


