package kr.kro.tripsketch.commons.utils

import io.github.cdimascio.dotenv.Dotenv

/**
 * `.env` 파일로부터 환경 변수들을 불러오는 유틸리티 객체입니다.
 *
 * 해당 객체는 `dotenv` 라이브러리를 활용하여 `.env` 파일 내에 설정된 속성들을 가져옵니다.
 * `.env` 파일은 프로젝트의 루트 디렉터리에 위치해야 합니다.
 *
 * @property dotenv 환경 변수에 접근하고 불러오는 것을 담당하는 Dotenv 클래스의 인스턴스입니다.
 * `.env` 파일 로딩에 실패할 경우 예외를 발생시킵니다.
 *
 * 사용 방법:
 * - 키를 사용하여 단일 속성 값 가져오기: `getProperty(key: String)`
 * - 키가 없을 경우 기본값으로 반환되는 속성 값 가져오기: `getPropertyOrDefault(key: String, defaultValue: String)`
 * - 모든 속성들을 맵 형식으로 가져오기: `getAllProperties()`
 * @author Hojun Song
 *
 */
object EnvLoader {
    private val dotenv: Dotenv by lazy {
        try {
            Dotenv.configure().directory(".").ignoreIfMissing().load()
        } catch (e: Exception) {
            throw IllegalStateException(".env 파일 로딩에 실패했습니다.", e)
        }
    }

    /**
     * 환경에서 지정된 속성 키의 값을 가져옵니다.
     *
     * @param key 속성의 이름.
     * @return 키와 연관된 값이나, 키를 찾지 못할 경우 `null`을 반환합니다.
     */
    fun getProperty(key: String): String? {
        return dotenv[key]
    }

    /**
     * 지정된 속성 키의 값을 가져오거나, 키가 없으면 기본값을 반환합니다.
     *
     * @param key 속성의 이름.
     * @param defaultValue 키를 찾지 못할 경우 반환되는 값.
     * @return 키와 연관된 값이나, 키를 찾지 못할 경우 기본값을 반환합니다.
     */
    fun getPropertyOrDefault(key: String, defaultValue: String): String {
        return dotenv[key] ?: defaultValue
    }

    /**
     * 모든 속성들을 맵 형식으로 반환합니다.
     *
     * @return 키와 값의 쌍으로 이루어진 맵을 반환합니다.
     */
    fun getAllProperties(): Map<String, String> {
        return dotenv.entries().associateBy({ it.key }, { it.value })
    }
}
