package kr.kro.tripsketch.config

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

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

    @Bean
    fun s3Presigner(): S3Presigner {
        return S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider {
                AwsBasicCredentials.create(accessKey, secretKey)
            }
            .endpointOverride(URI.create("https://compat.objectstorage.ap-osaka-1.oraclecloud.com"))
            .build()
    }
}
