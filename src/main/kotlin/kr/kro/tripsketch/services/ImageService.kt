package kr.kro.tripsketch.services

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.exif.ExifIFD0Directory
import org.im4java.core.ConvertCmd
import org.im4java.core.IMOperation
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.net.URI
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream


@Service
class ImageService(private val s3Service: S3Service) {

    private fun rotateImageIfNeeded(image: BufferedImage, file: MultipartFile): BufferedImage {
        try {
            val metadata: Metadata? = ImageMetadataReader.readMetadata(ByteArrayInputStream(file.bytes))
            val orientation = metadata?.getFirstDirectoryOfType(ExifIFD0Directory::class.java)?.getInt(ExifIFD0Directory.TAG_ORIENTATION)

            val transform = AffineTransform()
            when (orientation) {
                1 -> { /* Normal, no rotation needed. */ return image }
                2 -> { /* Flipped horizontally. */ transform.scale(-1.0, 1.0) }
                3 -> { /* Upside-down. */ transform.rotate(Math.PI) }
                4 -> { /* Flipped vertically. */ transform.scale(1.0, -1.0) }
                5 -> { transform.rotate(Math.PI / 2) ; transform.scale(-1.0, 1.0) }
                6 -> { transform.rotate(Math.PI / 2) }
                7 -> { transform.rotate(Math.PI / 2) ; transform.scale(1.0, -1.0) }
                8 -> { transform.rotate(-Math.PI / 2) }
                else -> { return image }
            }

            val op = AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR)
            return op.filter(image, null)
        } catch (e: Exception) {
            return image
        }
    }

    fun compressImage(file: MultipartFile): ByteArray {
        try {
            val formatName = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"

            // Create a temporary file to store the original image
            val tempOriginalFile = File.createTempFile("original_", ".$formatName")
            file.transferTo(tempOriginalFile)

            val outputPath = tempOriginalFile.absolutePath + "_compressed.$formatName"

            val imOperation = IMOperation()
            imOperation.addImage(tempOriginalFile.absolutePath)

            // Auto correct orientation
            imOperation.autoOrient()

            // Resize the image
            imOperation.resize(50, 50, "%")

            when (formatName.lowercase()) {
                "jpg", "jpeg" -> {
                    imOperation.quality(30.0) // set compression quality for JPEG
                }
                "png" -> {
                    imOperation.quality(30.0) // set compression quality for PNG (optional, but can be useful)
                }
            }

            imOperation.addImage(outputPath)
            val convertCmd = ConvertCmd()
            convertCmd.run(imOperation)

            // Read compressed image bytes
            val compressedBytes = File(outputPath).readBytes()

            // Cleanup temporary files
            tempOriginalFile.delete()
            File(outputPath).delete()

            return compressedBytes

        } catch (e: Exception) {
            // 압축 중 예외가 발생하면 원본 이미지 바이트를 반환
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
