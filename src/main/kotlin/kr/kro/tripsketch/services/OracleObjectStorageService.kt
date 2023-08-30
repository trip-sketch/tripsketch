package kr.kro.tripsketch.services

import org.springframework.stereotype.Service
import com.oracle.bmc.ConfigFileReader
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.requests.GetObjectRequest
import com.oracle.bmc.objectstorage.responses.GetObjectResponse
import kr.kro.tripsketch.utils.EnvLoader
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStream
import java.util.*

@Service
class OracleObjectStorageService {

    data class GetObjectResponse(val objectUrl: String?)

    fun uploadImageAndGetUrl(bucketName: String, file: MultipartFile): String {
        // OCI SDK 설정
        val oracleTenancy = EnvLoader.getProperty("ORACLE_TENANCY")
        val oracleBaseUrl = EnvLoader.getProperty("ORACLE_BASEURL")
        val ociConfigUser = EnvLoader.getProperty("OCI_CONFIG_USER")
        val ociConfigFingerprint = EnvLoader.getProperty("OCI_CONFIG_FINGERPRINT")
        val ociConfigTenancy = EnvLoader.getProperty("OCI_CONFIG_TENANCY")
        val ociConfigRegion = EnvLoader.getProperty("OCI_CONFIG_REGION")
        val ociConfigKeyfileEncoding = EnvLoader.getProperty("OCI_CONFIG_KEYFILE")
        val ociConfigKey = String(Base64.getDecoder().decode(ociConfigKeyfileEncoding))

        val configFile = "[DEFAULT]\nuser=$ociConfigUser\nfingerprint=$ociConfigFingerprint\ntenancy=$ociConfigTenancy\nregion=$ociConfigRegion\nkey_file=$ociConfigKey"

        val authDetailsProvider = ConfigFileAuthenticationDetailsProvider(configFile)
        println(" configFile: $authDetailsProvider")

        // Object Storage 클라이언트 생성
        val objectStorageClient = ObjectStorageClient.builder()
            .build(authDetailsProvider)
        println(" objectStorageClient: $objectStorageClient")

        try {
            // 파일 업로드
            val objectName: String = file.originalFilename ?: "example.txt"
            val objectData = String(file.bytes)
            val putObjectRequest = PutObjectRequest.builder()
                .namespaceName(oracleTenancy)
                .bucketName(bucketName)
                .objectName(objectName)
                .putObjectBody(objectData.byteInputStream())
                .build()
            objectStorageClient.putObject(putObjectRequest)

            // 파일 URL 생성
             val namespaceName = System.getenv("ORACLE_NAMESPACE")
             val getObjectRequest = GetObjectRequest.builder()
                 .namespaceName(namespaceName)
                 .bucketName(bucketName)
                 .objectName(objectName)
                 .build()

            val bucketUrl = oracleBaseUrl // 버킷 URL
            val objectUrl = "$bucketUrl/$objectName" // URL 조합

            println(objectUrl)

            return objectUrl

        } finally {
            // 연결 종료
            objectStorageClient.close()
        }
    }
}
