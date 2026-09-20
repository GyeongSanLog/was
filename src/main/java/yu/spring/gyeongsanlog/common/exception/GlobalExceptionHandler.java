package yu.spring.gyeongsanlog.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode().getErrorCode(), e.getErrorCode().getMessage()));
    }

    // DTO Validation 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst().orElse("잘못된 요청입니다.");
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", errorMessage));
    }

    /*
     쿼리 파라미터·경로 변수의 타입이 안 맞는 경우 (예: ?type=INVALID, /api/area/abc).
     여기서 잡지 않으면 스프링이 /error로 넘겨 응답 형식이 달라지고 스택트레이스까지 실린다.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = "'" + e.getName() + "' 값이 올바르지 않습니다: " + e.getValue();
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("INVALID_PARAMETER", message));
    }

    // 매핑되지 않은 경로
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("NOT_FOUND", "존재하지 않는 경로입니다."));
    }
}
