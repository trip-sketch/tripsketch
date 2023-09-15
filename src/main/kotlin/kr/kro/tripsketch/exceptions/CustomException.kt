package kr.kro.tripsketch.exceptions

import org.springframework.http.HttpStatus

/**
 * 사용자 정의 예외의 기본 클래스입니다.
 *
 * @property errorCode 에러 코드를 나타냅니다. ErrorCode enum 값 중 하나입니다.
 * @property message 예외 메시지입니다.
 * @author Hojun Song
 */
open class CustomException(private val errorCode: ErrorCode, override val message: String) : RuntimeException(message)

/**
 * HTTP 상태 코드를 갖는 에러 코드 enum입니다.
 */
enum class ErrorCode(val httpStatus: HttpStatus) {
    /**
     * 인증되지 않은 상태를 나타냅니다.
     */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),

    /**
     * 접근이 금지된 상태를 나타냅니다.
     */
    FORBIDDEN(HttpStatus.FORBIDDEN),

    /**
     * 잘못된 요청을 나타냅니다.
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST),

    /**
     * 데이터를 찾을 수 없을 때의 상태를 나타냅니다.
     */
    NOT_FOUND(HttpStatus.NOT_FOUND),

    /**
     * 서버 내부 에러를 나타냅니다.
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
}

/**
 * 인증되지 않은 상태에 대한 예외입니다.
 */
class UnauthorizedException(message: String) : CustomException(ErrorCode.UNAUTHORIZED, message)

/**
 * 접근이 금지된 상태에 대한 예외입니다.
 */
class ForbiddenException(message: String) : CustomException(ErrorCode.FORBIDDEN, message)

/**
 * 잘못된 요청에 대한 예외입니다.
 */
class BadRequestException(message: String) : CustomException(ErrorCode.BAD_REQUEST, message)

/**
 * 데이터를 찾을 수 없을 때의 예외입니다.
 */
class DataNotFoundException(message: String) : CustomException(ErrorCode.NOT_FOUND, message)

/**
 * 서버 내부 에러에 대한 예외입니다.
 */
class InternalServerException(message: String) : CustomException(ErrorCode.INTERNAL_SERVER_ERROR, message)
