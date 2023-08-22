package kr.kro.tripsketch.exceptions

import org.springframework.http.HttpStatus

open class CustomException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message)

enum class ErrorCode(val httpStatus: HttpStatus) {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),
    ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST), // 이 부분 추가
    // 기타 다른 오류 코드...
}


class UnauthorizedException(message: String) : CustomException(ErrorCode.UNAUTHORIZED, message)
class ForbiddenException(message: String) : CustomException(ErrorCode.FORBIDDEN, message)
class CustomExpiredTokenException(message: String) : CustomException(ErrorCode.TOKEN_EXPIRED, message)
class IllegalArgumentExceptionWrapper(message: String) : CustomException(ErrorCode.ILLEGAL_ARGUMENT, message)
class BadRequestException(message: String) : CustomException(ErrorCode.ILLEGAL_ARGUMENT, message