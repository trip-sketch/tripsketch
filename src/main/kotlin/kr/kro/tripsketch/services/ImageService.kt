package kr.kro.tripsketch.services

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.exif.ExifIFD0Directory
import org.im4java.core.ConvertCmd
import org.im4java.core.IMOperation
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.net.URI
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files


@Service
class ImageService(private val s3Service: S3Service, private val resourceLoader: ResourceLoader) {

    fun compressImage(file: MultipartFile): ByteArray {
        try {
            val formatName = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"

            // Use ResourceLoader to access the static folder
            val resource = resourceLoader.getResource("classpath:/static/")
            val staticDirPath = resource.file.absolutePath + "/"

            val tempOriginalFileName = "original_${System.currentTimeMillis()}.$formatName"
            val tempOriginalFilePath = staticDirPath + tempOriginalFileName
            val tempOriginalFile = File(tempOriginalFilePath)
            file.transferTo(tempOriginalFile)

            val outputPath = staticDirPath + tempOriginalFileName.substringBeforeLast('.') + "_compressed.$formatName"

            val imOperation = IMOperation()
            imOperation.addImage(tempOriginalFile.absolutePath)
            imOperation.autoOrient()
            imOperation.resize(50, 50, "%")
            when (formatName.lowercase()) {
                "jpg", "jpeg" -> imOperation.quality(25.0)
            }
            imOperation.addImage(outputPath)

            val convertCmd = ConvertCmd()
            convertCmd.run(imOperation)

            val compressedBytes = Files.readAllBytes(File(outputPath).toPath())

            tempOriginalFile.delete()
            File(outputPath).delete()

            return compressedBytes

        } catch (e: Exception) {
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
