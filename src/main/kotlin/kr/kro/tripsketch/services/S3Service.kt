package kr.kro.tripsketch.services

import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.core.sync.RequestBody
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class S3Service(private val s3Client: S3Client) {

    @Value("\${aws.bucketName}")
    lateinit var bucketName: String

    fun uploadFile(dir: String, multipartFile: MultipartFile): Pair<String, PutObjectResponse> {
        val datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val key = "$dir/$datetime${multipartFile.originalFilename}"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()

        val response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.bytes))

        return Pair(key, response)
    }

    fun deleteFile(key: String) {
        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()

        s3Client.deleteObject(deleteObjectRequest)
    }
}
