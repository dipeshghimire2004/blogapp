package org.blogapp.dg_blogapp.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blogapp.dg_blogapp.dto.ErrorResponse;
import org.blogapp.dg_blogapp.dto.GlobalApiResponse;
import org.blogapp.dg_blogapp.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for consistent error responses.
 */



@Slf4j
@RequiredArgsConstructor
@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(BaseException.class)
    public ResponseEntity<GlobalApiResponse<ErrorResponse>> handleBaseException(BaseException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setErrorCode(ErrorCode.valueOf(ex.getErrorCode().getCode()));
        HttpStatus status = HttpStatus.valueOf(ex.getErrorCode().getHttpStatus());
        log.error("Error {}: {}", ex.getErrorCode(), ex.getMessage(),ex);
        return ResponseEntity.status(status).body(GlobalApiResponse.error(errorResponse, status,ex.getMessage()));

    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<GlobalApiResponse<ErrorResponse>> handleUnauthorizedException(UnauthorizedException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Unauthorized access denied");
        errorResponse.setErrorCode(ErrorCode.valueOf(ex.getErrorCode().getCode()));
        log.error("Error {}: {}", ex.getErrorCode(), ex.getMessage(),ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GlobalApiResponse.error(errorResponse, HttpStatus.UNAUTHORIZED,"Unauthorized access denied"));
    }

    @ExceptionHandler(EncryptionException.class)
    public ResponseEntity<GlobalApiResponse<ErrorResponse>> handleEncryptionException(EncryptionException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Data security error occurred");
        errorResponse.setErrorCode(ErrorCode.ENCRYPTION_ERROR);
        log.error("Encryption error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalApiResponse.error(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR, "Data security error occurred"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());  // In production, customize to avoid leaking details
        body.put("path", request.getDescription(false));

        // Log the exception here (e.g., logger.error("Unhandled exception", ex));

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
//@ControllerAdvice
//public class GlobalExceptionHandler {
//    @ExceptionHandler(BlogPostNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handlePostNotFoundException(BlogPostNotFoundException ex) {
//        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
//        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
//    }
//    @ExceptionHandler(UserAlreadyExistsException.class)
//    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
//        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.CONFLICT);
//    }
//    @ExceptionHandler(UnauthorizedException.class)
//    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
//        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.FORBIDDEN);
//    }
//}
