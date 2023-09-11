package kr.kro.tripsketch

import kr.kro.tripsketch.services.OracleObjectStorageService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

@SpringBootTest
class TripsketchApplicationTests(

    @Autowired
    private val oracleObjectStorageService: OracleObjectStorageService,
) {

    @Test
    fun contextLoads() {
    }

    @Test
    fun testUploadImageAndGetUrl() {
        // 테스트용 파일 생성
        val bucketName = "tripsketch"
        val fileName = "example.txt"
        val content = "This is a test file content"
        val multipartFile: MultipartFile = MockMultipartFile(fileName, fileName, "text/plain", content.toByteArray())

        // 업로드하고 URL을 얻기
        val url = oracleObjectStorageService.uploadImageAndGetUrl(bucketName, multipartFile)
        System.out.println(url)
    }
}
