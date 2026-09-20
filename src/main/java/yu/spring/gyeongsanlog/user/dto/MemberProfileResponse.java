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
    private String name;
    private String profileImageUrl;
    private String provider;

    public static MemberProfileResponse from(User user) {
        return MemberProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .provider(user.getProvider().name())
                .build();
    }
}
