package kr.kro.tripsketch.commons.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

/**
 * S3 Bucket 설정을 위한 값 설정
 *
 * @author Hojun Song
 */
@Configuration
class S3Config {

    @Value("\${aws.accessKeyId}")
    lateinit var accessKey: String

    @Value("\${aws.secretAccessKey}")
    lateinit var secretKey: String

    @Value("\${aws.region}")
    lateinit var region: String

    @Bean
    fun s3Client(): S3Client {
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider {
                AwsBasicCredentials.create(accessKey, secretKey)
            }
            .endpointOverride(URI.create("https://compat.objectstorage.ap-osaka-1.oraclecloud.com"))
            .build()
    }
}
