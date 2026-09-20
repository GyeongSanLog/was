package yu.spring.gyeongsanlog.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "공통 에러 응답 형식")
public class ErrorResponse {
    @Schema(description = "애플리케이션 정의 예외 코드", example = "INVALID_HEADER")
    private String code;
    @Schema(description = "예외 메시지", example = "유효하지 않은 토큰입니다")
    private String message;
    @Schema(description = "에러 발생 시각", example = "2026-03-30T22:57:03")
    private LocalDateTime time;

    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .time(LocalDateTime.now())
                .build();
    }
}
