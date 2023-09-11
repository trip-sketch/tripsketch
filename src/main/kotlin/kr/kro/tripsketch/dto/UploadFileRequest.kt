package kr.kro.tripsketch.dto

import org.springframework.web.multipart.MultipartFile

data class UploadFileRequest(
    val file: MultipartFile,
)
