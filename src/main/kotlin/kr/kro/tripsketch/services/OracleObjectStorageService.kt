package kr.kro.tripsketch.services

import com.oracle.bmc.Region
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import org.springframework.stereotype.Service
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import kr.kro.tripsketch.utils.EnvLoader
import org.springframework.web.multipart.MultipartFile
import java.util.*
import java.io.InputStream
import java.io.ByteArrayInputStream
import java.util.function.Supplier

@Service
class OracleObjectStorageService {

    fun uploadImageAndGetUrl(bucketName: String, file: MultipartFile): String {
        // OCI SDK 설정
        val ociConfigUser = EnvLoader.getProperty("OCI_CONFIG_USER")
        val ociConfigFingerprint = EnvLoader.getProperty("OCI_CONFIG_FINGERPRINT")
        val ociConfigTenancy = EnvLoader.getProperty("OCI_CONFIG_TENANCY")
        val ociConfigKeyfileEncoding = EnvLoader.getProperty("OCI_CONFIG_KEYFILE")
        val ociConfigKey = String(Base64.getDecoder().decode(ociConfigKeyfileEncoding))

        val privateKeyStreamSupplier: Supplier<InputStream> = Supplier {
            ByteArrayInputStream(ociConfigKey.toByteArray(Charsets.UTF_8))
        }

        val authDetailsProvider: AuthenticationDetailsProvider = SimpleAuthenticationDetailsProvider.builder()
            .tenantId(ociConfigTenancy)
            .userId(ociConfigUser)
            .fingerprint(ociConfigFingerprint)
            .privateKeySupplier(privateKeyStreamSupplier)
            .build()

        println("authDetailsProvider: $authDetailsProvider")

        // Object Storage 클라이언트 생성
        val objectStorageClient = ObjectStorageClient.builder()
            .build(authDetailsProvider)
        println("objectStorageClient: $objectStorageClient")

        try {
            // 파일 업로드
            val objectName: String = file.originalFilename ?: "example.txt"
            val putObjectRequest = PutObjectRequest.builder()
                .namespaceName(ociConfigTenancy)
                .bucketName(bucketName)
                .objectName(objectName)
                .putObjectBody(file.inputStream)
                .build()
            objectStorageClient.putObject(putObjectRequest)

            // 파일 URL 생성
            val oracleBaseUrl = EnvLoader.getProperty("ORACLE_BASEURL")
            val objectUrl = "$oracleBaseUrl/$objectName" // URL 조합

            println(objectUrl)

            return objectUrl

        } finally {
            // 연결 종료
            objectStorageClient.close()
        }
    }
}
