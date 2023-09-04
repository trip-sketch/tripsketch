package kr.kro.tripsketch.dto

import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile


data class UserUpdateDto(
    @field:Size(min = 3, max = 50, message = "별명은 3자에서 50자 사이여야 합니다.")
    val nickname: String?,

    @field:Size(max = 500, message = "소개는 최대 500자까지 가능합니다.")
    val introduction: String?,

    val profileImageUrl: MultipartFile?
)