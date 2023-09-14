package kr.kro.tripsketch.services

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO
import java.util.*
import javax.imageio.IIOImage
import javax.imageio.ImageWriteParam


@Service
class ImageService(private val s3Service: S3Service) {

    fun compressImage(file: MultipartFile): ByteArray {
        try {
            val formatName = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"

            val originalImage: BufferedImage
            ByteArrayInputStream(file.bytes).use { bis ->
                originalImage = ImageIO.read(bis)
            }

            val width = Math.round(originalImage.width * 0.5).toInt()
            val height = Math.round(originalImage.height * 0.5).toInt()

            val compressedImageType = if (formatName.lowercase(Locale.getDefault()) in listOf("jpg", "jpeg")) BufferedImage.TYPE_INT_RGB else BufferedImage.TYPE_INT_ARGB
            val compressedImage = BufferedImage(width, height, compressedImageType)
            compressedImage.createGraphics().drawImage(originalImage, 0, 0, width, height, null)

            ByteArrayOutputStream().use { os ->
                when (formatName.lowercase(Locale.getDefault())) {
                    "jpg", "jpeg" -> {
                        val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
                        val param = writer.defaultWriteParam
                        param.compressionMode = ImageWriteParam.MODE_EXPLICIT
                        param.compressionQuality = 0.85f
                        writer.output = ImageIO.createImageOutputStream(os)
                        writer.write(null, IIOImage(compressedImage, null, null), param)
                    }
                    "png" -> {
                        ImageIO.write(compressedImage, "png", os)
                    }
                    else -> {
                        ImageIO.write(compressedImage, formatName, os)
                    }
                }
                return os.toByteArray()
            }
        } catch (e: Exception) {
            throw RuntimeException("이미지 압축 실패", e)
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
