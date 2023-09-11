package kr.kro.tripsketch.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.net.URLEncoder
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class S3Service(private val s3Client: S3Client, private val s3Presigner: S3Presigner) {

    @Value("\${aws.region}")
    lateinit var region: String

    @Value("\${aws.bucketName}")
    lateinit var bucketName: String

    fun uploadFile(dir: String, multipartFile: MultipartFile): Pair<String, PutObjectResponse> {
        val datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val key = "$dir/$datetime${multipartFile.originalFilename}"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build()

        val response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.bytes))

        val baseUrl = "https://objectstorage.${region}.oraclecloud.com/n/${bucketName}/b/${dir.split("/")[0]}/o"

        // encodedPathSuffix를 조건적으로 생성
        val encodedPathSuffix = if (dir.contains("/")) "${URLEncoder.encode(dir.split("/", limit = 2)[1], "UTF-8")}/" else ""

        val encodedFileName = URLEncoder.encode("$datetime${multipartFile.originalFilename}", "UTF-8")
        val url = "$baseUrl/$encodedPathSuffix$encodedFileName"

        return Pair(url, response)
    }


    fun getPresignedUrl(bucket: String, key: String, expiration: Duration): String {
        val getRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        val presignedGetObjectRequest = s3Presigner.presignGetObject(
            GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(getRequest)
                .build(),
        )

        return presignedGetObjectRequest.url().toString()
    }

    fun deleteFile(key: String) {
        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()

        s3Client.deleteObject(deleteObjectRequest)
    }
}
