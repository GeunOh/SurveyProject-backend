package com.surveyplus.creator.global.exception;

import com.surveyplus.creator.answer.exception.AnswerException;
import com.surveyplus.creator.global.jwt.TokenException;
import com.surveyplus.creator.survey.exception.QuestionException;
import com.surveyplus.creator.survey.exception.SurveyException;
import com.surveyplus.creator.user.exception.MemberException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDatabaseException(DataIntegrityViolationException e) {
        log.error("DB Error: ", e);

        // 클라이언트에게는 알기 쉬운 메시지 반환
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<Void>("DB_ERROR", "데이터 처리 중 오류가 발생했습니다.", null));
    }

    // 1. @Valid 유효성 검사 실패 시
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        // 첫 번째 에러 메시지만 가져오거나, 모든 에러를 모아서 전달
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("valid 에러: {}", message);
        return ResponseEntity.badRequest().body(new ApiResponse<Void>("VF", message, null));
    }

    // 💡 JWT 또는 인증 관련 예외 공통 처리
    @ExceptionHandler({
            io.jsonwebtoken.ExpiredJwtException.class,
            io.jsonwebtoken.security.SignatureException.class,
            io.jsonwebtoken.MalformedJwtException.class,
            io.jsonwebtoken.UnsupportedJwtException.class,
            IllegalArgumentException.class,
    })
    public ResponseEntity<ApiResponse<Void>> handleJwtException(Exception e) {
        log.error("JWT 인증 에러: ", e);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<Void>("IVT001", "로그인 정보가 만료되었습니다. 다시 로그인해 주세요.", null));
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenException(TokenException e) {
        log.error("User 에러: ", e);

        BaseErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ApiResponse<Void>(errorCode.getCode(), errorCode.getMessage(), null));
    }

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserException(MemberException e) {
        log.error("User 에러: ", e);

        BaseErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ApiResponse<Void>(errorCode.getCode(), errorCode.getMessage(), null));
    }

    @ExceptionHandler(SurveyException.class)
    public ResponseEntity<ApiResponse<Void>> handleSurveyException(SurveyException e) {
        log.error("Survey 에러: ", e);

        BaseErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ApiResponse<Void>(errorCode.getCode(), errorCode.getMessage(), null));
    }

    @ExceptionHandler(AnswerException.class)
    public ResponseEntity<ApiResponse<Void>> handleAnswerException(AnswerException e) {
        log.error("Answer 에러: ", e);

        BaseErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ApiResponse<Void>(errorCode.getCode(), errorCode.getMessage(), null));
    }

    @ExceptionHandler(QuestionException.class)
    public ResponseEntity<ApiResponse<Void>> handleQuestionException(QuestionException e) {
        log.error("Question 에러: ", e);

        BaseErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ApiResponse<Void>(errorCode.getCode(), errorCode.getMessage(), null));
    }
}
