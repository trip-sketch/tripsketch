package kr.kro.tripsketch.utils

class PagenationUtil {
    fun validatePageAndSize(page: Int?, size: Int?): Pair<Int, Int> {
        val validatedPage = try {
            if (page != null && page > 0) page else 1
        } catch (e: NumberFormatException) {
            1
        }

        val validatedSize = try {
            if (size != null && size > 0) size else 10
        } catch (e: NumberFormatException) {
            10
        }
        return Pair(validatedPage, validatedSize)
    }
}