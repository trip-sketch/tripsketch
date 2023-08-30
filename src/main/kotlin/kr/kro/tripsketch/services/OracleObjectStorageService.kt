package kr.kro.tripsketch.services

import org.springframework.stereotype.Service
import com.oracle.bmc.ConfigFileReader
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.requests.GetObjectRequest
import com.oracle.bmc.objectstorage.responses.GetObjectResponse
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStream

@Service
class OracleObjectStorageService {

    data class GetObjectResponse(val objectUrl: String?)

    fun uploadImageAndGetUrl(bucketName: String, file: MultipartFile): String {
        // OCI SDK 설정
        val oracleTenancy = "ORACLE_TENANCY"
        val oracleBaseUrl = "ORACLE_BASEURL"
        val configFile = ConfigFileReader.parse("~/.oci/config")
        val authDetailsProvider = ConfigFileAuthenticationDetailsProvider(configFile)

        // Object Storage 클라이언트 생성
        val objectStorageClient = ObjectStorageClient.builder()
            .build(authDetailsProvider)

        try {
            // 파일 업로드
            val objectName: String = file.originalFilename ?: "example.txt"
            val objectData = String(file.bytes)
            val putObjectRequest = PutObjectRequest.builder()
                .namespaceName(configFile.get(oracleTenancy))
                .bucketName(bucketName)
                .objectName(objectName)
                .putObjectBody(objectData.byteInputStream())
                .build()
            objectStorageClient.putObject(putObjectRequest)

            // 파일 URL 생성
//            val namespaceName = System.getenv("ORACLE_NAMESPACE")
//            val getObjectRequest = GetObjectRequest.builder()
//                .namespaceName(namespaceName)
//                .bucketName(bucketName)
//                .objectName(objectName)
//                .build()

            val bucketUrl = oracleBaseUrl // 버킷 URL
            val objectUrl = "$bucketUrl/$objectName" // URL 조합

            System.out.println(objectUrl)

            return objectUrl

        } finally {
            // 연결 종료
            objectStorageClient.close()
        }
    }
}
