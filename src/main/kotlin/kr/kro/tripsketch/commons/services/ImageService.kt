package kr.kro.tripsketch.commons.services

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@Service
class ImageService(private val s3Service: S3Service) {

    /**
     * 이미지를 S3에 업로드하고, 해당 이미지의 URL을 반환합니다.
     *
     * @param dir S3에 이미지를 저장할 디렉토리 경로.
     * @param file 업로드할 이미지 파일.
     * @return 업로드된 이미지의 URL.
     * @author Hojun Song
     */

    fun uploadImage(dir: String, file: MultipartFile): String {
        val (url, _) = s3Service.uploadFile(dir, file)
        return url
    }

    /**
     * 주어진 이미지의 URL을 기반으로 S3에서 해당 이미지를 삭제합니다.
     *
     * @param url 삭제할 이미지의 URL.
     */
    fun deleteImage(url: String) {
        val (dir, key) = extractDirAndKeyFromUrl(url)
        s3Service.deleteFile(dir, key)
    }

    /**
     * 주어진 URL에서 디렉토리와 키 값을 추출합니다.
     *
     * @param url 정보를 추출할 URL.
     * @return 디렉토리와 키 값을 포함한 Pair 객체.
     */
    private fun extractDirAndKeyFromUrl(url: String): Pair<String, String> {
        val path = URI(url).path
        val parts = path.split('/')

        val dir = "${parts[4]}/${parts[6]}"
        val key = parts[7]

        return Pair(dir, key)
    }
}
