package kr.kro.tripsketch.services

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.net.URL

@Service
class ImageService(private val s3Service: S3Service) {

    fun uploadImage(dir: String, file: MultipartFile): String {
        val (url, _) = s3Service.uploadFile(dir, file)
        return url
    }

    fun deleteImage(url: String) {
        val key = extractKeyFromUrl(url)
        s3Service.deleteFile(key)
    }

    private fun extractKeyFromUrl(url: String): String {
        val path = URL(url).path
        val segments = path.split("/")
        if (segments.size < 5) return "" // 무효한 URL
        return segments.drop(5).joinToString("/")
    }

}
