package kr.kro.tripsketch.exceptions

import org.springframework.http.HttpStatus

open class CustomException(
    val errorCode: ErrorCode,
    override val message: String,
) : RuntimeException(message)

enum class ErrorCode(val httpStatus: HttpStatus) {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    BAD_REQUEST(HttpStatus.BAD_REQUEST), // 이 부분 추가,
    NOT_FOUND(HttpStatus.NOT_FOUND),
}

class UnauthorizedException(message: String) : CustomException(ErrorCode.UNAUTHORIZED, message)
class ForbiddenException(message: String) : CustomException(ErrorCode.FORBIDDEN, message)
class BadRequestException(message: String) : CustomException(ErrorCode.BAD_REQUEST, message)
class DataNotFoundException(message: String) : CustomException(ErrorCode.NOT_FOUND, message)
