package kr.kro.tripsketch.services

import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.core.sync.RequestBody
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

@Service
class S3Service(private val s3Client: S3Client) {

    init {
        disableSslVerification()
    }

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

    private fun disableSslVerification() {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOfNulls(0)
            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        SSLContext.setDefault(sslContext)
    }
}
