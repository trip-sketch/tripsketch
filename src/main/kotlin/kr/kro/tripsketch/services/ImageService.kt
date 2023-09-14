package kr.kro.tripsketch.services

import javax.imageio.plugins.jpeg.JPEGImageWriteParam
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import ar.com.hjg.pngj.*


@Service
class ImageService(private val s3Service: S3Service) {

    fun compressImage(file: MultipartFile): ByteArray {
        try {
            val formatName = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"

            val originalImage: BufferedImage
            ByteArrayInputStream(file.bytes).use { bis ->
                originalImage = ImageIO.read(bis)
            }

            val width = originalImage.width / 2
            val height = originalImage.height / 2

            val compressedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            compressedImage.createGraphics().drawImage(originalImage, 0, 0, width, height, null)

            ByteArrayOutputStream().use { os ->
                when {
                    formatName.equals("jpg", true) || formatName.equals("jpeg", true) -> {
                        val writer = ImageIO.getImageWritersByFormatName(formatName).next()
                        val writeParam = writer.defaultWriteParam

                        if (writeParam is JPEGImageWriteParam) {
                            writeParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
                            writeParam.compressionQuality = 0.5f
                        }

                        ImageIO.createImageOutputStream(os).use { ios ->
                            writer.output = ios
                            writer.write(null, IIOImage(compressedImage, null, null), writeParam)
                        }
                    }
                    formatName.equals("png", true) -> {
                        // PNG 압축 로직
                        val imi = ImageInfo(width, height, 8, false)
                        val pngWriter = PngWriter(os, imi)
                        val db = ImageLineInt(imi)
                        for (row in 0 until height) {
                            for (col in 0 until width) {
                                val pixel = compressedImage.getRGB(col, row)
                                db.scanline[col] = pixel
                            }
                            pngWriter.writeRow(db, row)
                        }
                        pngWriter.end()
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
