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
        val (dir, key) = extractDirAndKeyFromUrl(url)
        s3Service.deleteFile(dir, key)
    }

    private fun extractDirAndKeyFromUrl(url: String): Pair<String, String> {
        val path = URL(url).path
        val parts = path.split('/')

        val dir = "${parts[4]}/${parts[6]}"
        val key = parts[7]

        return Pair(dir, key)
    }

}
