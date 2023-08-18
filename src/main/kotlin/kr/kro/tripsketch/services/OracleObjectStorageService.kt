package kr.kro.tripsketch.services

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import com.oracle.bmc.ConfigFileReader
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.ObjectStorage
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration
import com.oracle.bmc.objectstorage.transfer.UploadManager
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadRequest
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadResponse
import kr.kro.tripsketch.repositories.UserRepository
import java.io.InputStream

@Service
class OracleObjectStorageService {
    val namespaceName = "oracle.objectstorage.bucketname"
    val bucketName = "oracle.objectstorage.namespace"
    val configurationFilePath = "~/resources/config"
    val profile = "DEFAULT"

    val configFile = ConfigFileReader.parse(configurationFilePath, profile)
    val provider = ConfigFileAuthenticationDetailsProvider(configFile)
    val client: ObjectStorage = ObjectStorageClient(provider) // 여기서 바로 초기화
    val uploadConfiguration = UploadConfiguration.builder()
            .allowMultipartUploads(true)
            .allowParallelUploads(true)
            .build()
    val uploadManager: UploadManager = UploadManager(client, uploadConfiguration)
    fun uploadImageAndGetUrl(file: MultipartFile): String {
        val objectName = file.originalFilename // 오리지널 파일 이름을 객체 이름으로 사용

        val inputStream: InputStream = file.inputStream

        val request = PutObjectRequest.builder()
                .bucketName(bucketName)
                .namespaceName(namespaceName)
                .objectName(objectName)
                .contentType(file.contentType)
                .contentLength(file.size)
                .build()

        val uploadDetails = UploadRequest.builder()
                .allowOverwrite(true)
                .putObjectRequest(request)
                .build(inputStream)

        // 업로드 요청 및 응답 받기
        val response: UploadResponse = uploadManager.upload(uploadDetails)

        // InputStream 정리
        inputStream.close()

        // 객체 URL 가져오기
        val objectUrl = getObjectUrl(objectName)

        return objectUrl
    }

//    fun downloadImageAndGetUrl(objectName: String): String {
//        val request = GetObjectRequest.builder()
//                .bucketName(bucketName)
//                .namespaceName(namespaceName)
//                .objectName(objectName)
//                .build()
//
//        val response: GetObjectResponse = client.getObject(request)
//
//        // 다운로드된 객체의 InputStream 가져오기
//        val inputStream: InputStream = response.inputStream
//
//        // InputStream을 파일로 저장하거나, 다운로드할 수 있는 URL을 생성하는 등의 작업을 수행할 수 있습니다.
//
//        // InputStream 정리
//        inputStream.close()
//
//        // 객체 URL 가져오기
//        val objectUrl = getObjectUrl(objectName)
//
//        return objectUrl
//    }
}