package yu.spring.gyeongsanlog.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "카카오 소셜 로그인 요청")
public class KakaoLoginRequest {

    @Schema(description = "카카오에서 발급받은 인가 코드", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String authCode;

    // 카카오가 인가 코드를 검증할 때 1단계에서 쓴 값과 대조하므로 프론트가 사용한 주소를 그대로 받는다
    @Schema(description = "인가 코드를 받을 때 사용한 redirect_uri (프론트엔드 주소)",
            example = "http://localhost:5173/oauth/kakao", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String redirectUrl;
}
