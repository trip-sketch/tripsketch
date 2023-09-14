package kr.kro.tripsketch.services

import com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReader
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
import javax.imageio.stream.ImageInputStream


@Service
class ImageService(private val s3Service: S3Service) {

    fun compressImage(file: MultipartFile): ByteArray {
        val formatName = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"

        // Read the image. If you have a specialized method to read JPEG, you can use it here.
        // For this example, I'm assuming ImageIO.read works for all types.
        val originalImage: BufferedImage = ImageIO.read(ByteArrayInputStream(file.bytes))

        val width = originalImage.width / 2
        val height = originalImage.height / 2

        val compressedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        compressedImage.createGraphics().drawImage(originalImage, 0, 0, width, height, null)

        val os = ByteArrayOutputStream()

        if (formatName.equals("jpg", true) || formatName.equals("jpeg", true)) {
            val writer = ImageIO.getImageWritersByFormatName(formatName).next()
            val writeParam = writer.defaultWriteParam

            if (writeParam is JPEGImageWriteParam) {
                writeParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
                writeParam.compressionQuality = 0.5f
            }

            writer.output = ImageIO.createImageOutputStream(os)
            writer.write(null, IIOImage(compressedImage, null, null), writeParam)
        } else {
            ImageIO.write(compressedImage, formatName, os)
        }

        return os.toByteArray()
    }


    private fun readJPEGImage(input: InputStream): BufferedImage {
        val imageReader = ImageIO.getImageReadersByFormatName("JPEG").next() as JPEGImageReader
        val imageInputStream: ImageInputStream = ImageIO.createImageInputStream(input)
        imageReader.setInput(imageInputStream, true)
        return imageReader.read(0)
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
