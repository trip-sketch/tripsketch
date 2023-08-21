package kr.kro.tripsketch.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TokenValidation(
    val adminOnly: Boolean = false // 관리자만 접근 가능한 API인지 표시하는 속성
)
