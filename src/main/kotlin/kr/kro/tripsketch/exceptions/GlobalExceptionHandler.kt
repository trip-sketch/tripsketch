package kr.kro.tripsketch.exceptions

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<String> {
        return ResponseEntity.status(e.errorCode.httpStatus).body(e.message)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<String> {
        val wrappedException = IllegalArgumentExceptionWrapper(e.message ?: "Invalid Argument")
        return handleCustomException(wrappedException)
    }

    // 기타 다른 예외 처리 메서드...
}
