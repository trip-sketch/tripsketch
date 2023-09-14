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


@Service
class ImageService(private val s3Service: S3Service) {

    fun compressImage(file: MultipartFile): ByteArray {
        try {
            val formatName = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"

            val originalImage: BufferedImage
            ByteArrayInputStream(file.bytes).use { bis ->
                originalImage = ImageIO.read(bis)
            }

            // Convert the image to RGB colorspace if it's not.
            val convertedImage = if (originalImage.type != BufferedImage.TYPE_INT_RGB && originalImage.type != BufferedImage.TYPE_INT_ARGB) {
                val rgbImage = BufferedImage(originalImage.width, originalImage.height, BufferedImage.TYPE_INT_RGB)
                rgbImage.createGraphics().apply {
                    drawImage(originalImage, 0, 0, null)
                    dispose()
                }
                rgbImage
            } else {
                originalImage
            }

            val width = Math.round(convertedImage.width * 0.5).toInt()
            val height = Math.round(convertedImage.height * 0.5).toInt()

            val compressedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            compressedImage.createGraphics().drawImage(convertedImage, 0, 0, width, height, null)

            ByteArrayOutputStream().use { os ->
                when (formatName.lowercase(Locale.getDefault())) {
                    "jpg", "jpeg" -> {
                        ImageIO.write(compressedImage, "jpg", os)
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
            throw RuntimeException("Image compression failed", e)
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
