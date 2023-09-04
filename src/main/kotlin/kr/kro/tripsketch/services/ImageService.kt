package kr.kro.tripsketch.services

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ImageService(private val s3Service: S3Service) {

    fun uploadImage(directory: String, file: MultipartFile): String {
        val (key, response) = s3Service.uploadFile(directory, file)
        return key
    }

    fun deleteImage(key: String) {
        s3Service.deleteFile(key)
    }
}
