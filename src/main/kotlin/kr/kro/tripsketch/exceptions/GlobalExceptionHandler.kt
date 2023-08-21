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

    // 기타 다른 예외 처리 메서드...
}