package kr.kro.tripsketch.utils

/**
 * 페이지네이션할 때 사용할 수 있는 유틸리티입니다.
 * page 와 size 값을 검증하여 반환합니다.
 *   - page: 불러올 페이지
 *   - size: 페이지 당 조회될 데이터 수
 *   - 기본값은 page: 1 / size: 10 입니다.
 *   - params에 해당 값을 입력을 하지 않을 시, 기본값으로 설정됩니다.
 * */
class PagenationUtil {
    fun validatePageAndSize(page: Int?, size: Int?): Pair<Int, Int> {

        /** 불러올 페이지 검증 */
        val validatedPage = try {
            if (page != null && page > 0) page else 1
        } catch (e: NumberFormatException) {
            1
        }

        /** 페이지 당 조회될 데이터 갯수 검증 */
        val validatedSize = try {
            if (size != null && size > 0) size else 10
        } catch (e: NumberFormatException) {
            10
        }
        return Pair(validatedPage, validatedSize)
    }
}
