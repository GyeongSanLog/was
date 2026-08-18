package yu.spring.gyeongsanlog.user.dto;

import lombok.Builder;
import lombok.Getter;
import yu.spring.gyeongsanlog.user.domain.User;

@Getter
@Builder
public class MemberProfileResponse {
    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String provider;

    public static MemberProfileResponse from(User user) {
        return MemberProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .provider(user.getProvider().name())
                .build();
    }
}
