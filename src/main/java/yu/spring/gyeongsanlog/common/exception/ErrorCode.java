package yu.spring.gyeongsanlog.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 가입된 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 사용자입니다."),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "GROUP_NOT_FOUND", "존재하지 않는 그룹입니다."),
    ALREADY_GROUP_MEMBER(HttpStatus.CONFLICT, "ALREADY_GROUP_MEMBER", "이미 참여중인 그룹입니다."),
    LEADER_CANNOT_LEAVE(HttpStatus.CONFLICT, "LEADER_CANNOT_LEAVE", "다른 멤버가 있는 동안에는 리더가 탈퇴할 수 없습니다."),
    DUPLICATE_CLIP_SLOT(HttpStatus.CONFLICT, "DUPLICATE_CLIP_SLOT", "해당 시간대에 이미 업로드한 클립이 있습니다."),
    INVALID_CAPTURED_AT(HttpStatus.BAD_REQUEST, "INVALID_CAPTURED_AT", "촬영 시각이 그룹 시작 시각보다 이전일 수 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "DUPLICATE_NICKNAME", "이미 사용중인 닉네임입니다."),

    FILE_EMPTY(HttpStatus.BAD_REQUEST, "FILE_EMPTY", "파일이 비어있습니다."),
    FILENAME_MISSING(HttpStatus.BAD_REQUEST, "FILENAME_MISSING", "파일 이름이 없습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "지원하지 않는 파일 형식입니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE", "파일 용량이 너무 큽니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED", "파일 업로드에 실패했습니다."),

    TOUR_API_FAILED(HttpStatus.BAD_GATEWAY, "TOUR_API_FAILED", "관광정보 API 호출에 실패했습니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_NOT_FOUND", "존재하지 않는 관광지입니다."),
    RECOMMENDATION_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "RECOMMENDATION_INVALID_PARAMETER",
            "placeId와 category 중 하나만 입력해야 합니다.");

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String errorCode, String message) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.message = message;
    }
}
