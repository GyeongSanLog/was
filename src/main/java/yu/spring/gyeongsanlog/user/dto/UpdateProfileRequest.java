package yu.spring.gyeongsanlog.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateProfileRequest {
    @NotBlank
    private String nickname;

    @NotBlank
    private String name;

    // true면 프로필 사진을 기본 이미지(null)로 되돌림
    private boolean resetProfileImage;
}
