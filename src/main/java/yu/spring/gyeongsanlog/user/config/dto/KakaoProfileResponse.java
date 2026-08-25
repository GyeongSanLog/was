package yu.spring.gyeongsanlog.user.config.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoProfileResponse {

    // 카카오 회원번호. 우리 쪽 User.providerId로 저장한다.
    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    // 동의항목 검수 전이거나 사용자가 거부하면 email/profile이 통째로 없을 수 있다
    public String getEmailOrNull() {
        return kakaoAccount == null ? null : kakaoAccount.getEmail();
    }

    public String getNicknameOrNull() {
        if (kakaoAccount == null || kakaoAccount.getProfile() == null) {
            return null;
        }
        return kakaoAccount.getProfile().getNickname();
    }

    public String getProfileImageUrlOrNull() {
        if (kakaoAccount == null || kakaoAccount.getProfile() == null) {
            return null;
        }
        return kakaoAccount.getProfile().getProfileImageUrl();
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        private String email;
        private Profile profile;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        private String nickname;

        @JsonProperty("profile_image_url")
        private String profileImageUrl;
    }
}
