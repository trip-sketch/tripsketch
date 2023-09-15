package kr.kro.tripsketch.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * 전역 예외 처리기입니다.
 * 해당 핸들러는 애플리케이션에서 발생하는 예외들을 처리합니다.
 * @author Hojun Song
 */
@ControllerAdvice
class GlobalExceptionHandler {

    /**
     * UnauthorizedException 예외를 처리합니다.
     */
    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(e: UnauthorizedException): ResponseEntity<String> {
        return ResponseEntity(e.message, HttpStatus.UNAUTHORIZED)
    }

    /**
     * ForbiddenException 예외를 처리합니다.
     */
    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(e: ForbiddenException): ResponseEntity<String> {
        return ResponseEntity(e.message, HttpStatus.FORBIDDEN)
    }

    /**
     * BadRequestException 예외를 처리합니다.
     */
    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(e: BadRequestException): ResponseEntity<String> {
        return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
    }

    /**
     * IllegalArgumentException 예외를 처리합니다.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
    }

    /**
     * IllegalStateException 예외를 처리합니다.
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<String> {
        return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
    }

    /**
     * MethodArgumentNotValidException 예외를 처리하며, 유효성 검사 오류를 매핑합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(e: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errors = e.bindingResult.fieldErrors.associate {
            it.field to it.defaultMessage!!
        }
        return ResponseEntity(errors, HttpStatus.BAD_REQUEST)
    }

    /**
     * DataNotFoundException 예외를 처리합니다.
     */
    @ExceptionHandler(DataNotFoundException::class)
    fun handleDataNotFoundException(e: DataNotFoundException): ResponseEntity<String> {
        return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
    }

    /**
     * InternalServerException 예외를 처리합니다.
     */
    @ExceptionHandler(InternalServerException::class)
    fun handleInternalServerException(e: InternalServerException): ResponseEntity<String> {
        return ResponseEntity(e.message, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
