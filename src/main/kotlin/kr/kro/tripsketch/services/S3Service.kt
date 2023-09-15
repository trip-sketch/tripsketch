package kr.kro.tripsketch.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class S3Service(private val s3Client: S3Client) {

    @Value("\${aws.region}")
    lateinit var region: String

    @Value("\${aws.bucketName}")
    lateinit var bucketName: String

    fun uploadFile(dir: String, multipartFile: MultipartFile): Pair<String, PutObjectResponse> {
        val datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val originalFilenameWithoutExtension = multipartFile.originalFilename?.substringBeforeLast(".") ?: "file"
        val fileExtension = multipartFile.originalFilename?.substringAfterLast(".", "")

        val encodedFileNameWithoutExtension = Base64.getEncoder().encodeToString(originalFilenameWithoutExtension.toByteArray(Charsets.UTF_8)).replace("=", "")
        val newFilename = "$datetime$encodedFileNameWithoutExtension.$fileExtension"
        val key = "$dir/$newFilename"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build()

        val response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.bytes))

        // URL 구조 변경
        val baseUrl = "https://$bucketName.objectstorage.$region.oci.customer-oci.com/n/$bucketName/b/${dir.split("/")[0]}/o"
        val encodedPathSuffix = if (dir.contains("/")) "${URLEncoder.encode(dir.split("/", limit = 2)[1], "UTF-8")}/" else ""
        val url = "$baseUrl/$encodedPathSuffix$newFilename"

        return Pair(url, response)
    }

    fun deleteFile(dir: String, key: String) {
        val fullPath = "$dir/$key"

        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(fullPath)
            .build()

        s3Client.deleteObject(deleteObjectRequest)
    }

    fun downloadBytes(dir: String, fileName: String): ByteArray {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key("$dir/$fileName")
            .build()

        val response = s3Client.getObject(getObjectRequest)

        return response.readAllBytes()
    }
}
