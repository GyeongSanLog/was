package yu.spring.gyeongsanlog.group.dto;

import lombok.Builder;
import lombok.Getter;
import yu.spring.gyeongsanlog.group.domain.GroupMember;

@Getter
@Builder
public class GroupMemberResponse {
    private Long userId;
    private String nickname;
    private String profileImageUrl;

    public static GroupMemberResponse from(GroupMember groupMember) {
        return GroupMemberResponse.builder()
                .userId(groupMember.getUser().getId())
                .nickname(groupMember.getUser().getNickname())
                .profileImageUrl(groupMember.getUser().getProfileImageUrl())
                .build();
    }
}
