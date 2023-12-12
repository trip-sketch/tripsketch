package kr.kro.tripsketch.commons.services

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

    /**
     * 파일을 S3 저장소에 업로드하고 해당 파일의 URL과 응답을 반환합니다.
     *
     * 1. 현재 시간을 기반으로 파일명을 생성합니다.
     * 2. 원본 파일명의 확장자를 제외한 부분을 Base64로 인코딩합니다.
     * 3. 새로운 파일명을 생성합니다. (현재시간 + 인코딩된 파일명 + 확장자)
     * 4. S3에 업로드할 key(경로)를 정의합니다.
     * 5. 업로드 요청 객체를 생성합니다.
     * 6. 파일을 S3에 업로드하고, 그 응답을 받습니다.
     * 7. 파일의 URL을 구성하고 반환합니다.
     *
     * @param dir S3에 파일을 저장할 디렉토리 경로
     * @param multipartFile 업로드할 MultipartFile 객체
     * @return 업로드된 파일의 URL과 S3 응답 객체의 쌍
     * @author Hojun Song
     */

    fun uploadFile(dir: String, multipartFile: MultipartFile): Pair<String, PutObjectResponse> {
        val datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val fileExtension = multipartFile.originalFilename?.substringAfterLast(".", "") ?: ""

        val uniqueId = UUID.randomUUID().toString()

        val newFilename = "$datetime-$uniqueId.$fileExtension"
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

    /** 서버에 등록된 파일을 삭제하는 함수 */
    fun deleteFile(dir: String, key: String) {
        val fullPath = "$dir/$key"

        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(fullPath)
            .build()

        s3Client.deleteObject(deleteObjectRequest)
    }
}
