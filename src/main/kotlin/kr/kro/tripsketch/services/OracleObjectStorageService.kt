package kr.kro.tripsketch.services

import java.io.InputStream
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*
import java.io.ByteArrayInputStream
import java.util.function.Supplier

@Service
class OracleObjectStorageService {

    @Value("\${OCI_CONFIG_TENANCY}")
    private lateinit var ociConfigTenancy: String

    @Value("\${OCI_CONFIG_USER}")
    private lateinit var ociConfigUser: String

    @Value("\${OCI_CONFIG_FINGERPRINT}")
    private lateinit var ociConfigFingerprint: String

    @Value("\${OCI_CONFIG_KEYFILE}")
    private lateinit var ociConfigKeyfile: String

    @Value("\${ORACLE_BASEURL}")
    private lateinit var oracleBaseURL: String

    @Value("\${ORACLE_NAMESPACE}")
    private lateinit var namespaceName: String

    private fun getProvider(): SimpleAuthenticationDetailsProvider {
        val decodedKey = Base64.getDecoder().decode(ociConfigKeyfile)
        val privateKeySupplier = Supplier<InputStream> { ByteArrayInputStream(decodedKey) }

        return SimpleAuthenticationDetailsProvider.builder()
            .tenantId(ociConfigTenancy)
            .userId(ociConfigUser)
            .fingerprint(ociConfigFingerprint)
            .privateKeySupplier(privateKeySupplier)
            .build()
    }

    private val client: ObjectStorageClient by lazy {
        // Builder 인스턴스 생성
        val builder = ObjectStorageClient.builder()

        // Builder를 사용하여 ObjectStorageClient 인스턴스를 생성합니다.
        builder.build(getProvider())
    }


    fun uploadImageAndGetUrl(bucketName: String, file: MultipartFile): Pair<String?, String?> {
        val objectName = file.originalFilename!!

        val request = PutObjectRequest.builder()
            .bucketName(bucketName)
            .namespaceName(namespaceName)
            .objectName(objectName)
            .putObjectBody(file.inputStream)
            .build()

        return try {
            val response = client.putObject(request)
            Pair("$oracleBaseURL$objectName", null) // First is URL, second is error
        } catch (e: Exception) {
            Pair(null, e.message) // In case of an error, return the error message
        }
    }
}

