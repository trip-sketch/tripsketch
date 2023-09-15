package kr.kro.tripsketch.services

import net.coobird.thumbnailator.Thumbnails
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
d

@Service
class ImageService(private val s3Service: S3Service) {

    fun compressImage(file: MultipartFile): ByteArray {
        try {
            val formatName = file.originalFilename?.substringAfterLast('.', "")?.lowercase() ?: "jpg"

            val outputStream = ByteArrayOutputStream()

            val thumbnailBuilder = Thumbnails.of(file.inputStream).size(1080, 1080).keepAspectRatio(true)

            when (formatName) {
                "jpg", "jpeg" -> thumbnailBuilder.outputQuality(0.25)  // JPG, JPEG는 25% 품질로 설정
                else -> thumbnailBuilder.outputQuality(0.5)  // 다른 형식은 50% 품질로 설정
            }

            thumbnailBuilder
                .outputFormat(formatName)
                .toOutputStream(outputStream)

            return outputStream.toByteArray()

        } catch (e: Exception) {
            e.printStackTrace()
            return file.bytes
        }
    }



    fun uploadImage(dir: String, file: MultipartFile): String {
        val compressedImageBytes = compressImage(file)

        val compressedFile = ByteArrayMultipartFile(
            file.name,
            file.originalFilename ?: "",
            file.contentType,
            compressedImageBytes
        )

        val (url, _) = s3Service.uploadFile(dir, compressedFile)
        return url
    }

    fun deleteImage(url: String) {
        val (dir, key) = extractDirAndKeyFromUrl(url)
        s3Service.deleteFile(dir, key)
    }

    private fun extractDirAndKeyFromUrl(url: String): Pair<String, String> {
        val path = URI(url).path
        val parts = path.split('/')

        val dir = "${parts[4]}/${parts[6]}"
        val key = parts[7]

        return Pair(dir, key)
    }

    class ByteArrayMultipartFile(
        private val fileName: String,
        private val originalFileName: String,
        private val contentType: String?,
        private val content: ByteArray
    ) : MultipartFile {
        override fun getName(): String = fileName

        override fun getOriginalFilename(): String = originalFileName

        override fun getContentType(): String? = contentType

        override fun isEmpty(): Boolean = content.isEmpty()

        override fun getSize(): Long = content.size.toLong()

        override fun getBytes(): ByteArray = content

        override fun getInputStream(): InputStream = ByteArrayInputStream(content)

        override fun transferTo(dest: File) {
            dest.writeBytes(content)
        }
    }

}
