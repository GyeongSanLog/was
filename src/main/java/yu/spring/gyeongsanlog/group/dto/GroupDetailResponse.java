package yu.spring.gyeongsanlog.group.dto;

import lombok.Builder;
import lombok.Getter;
import yu.spring.gyeongsanlog.group.domain.TravelGroup;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class GroupDetailResponse {
    private Long id;
    private String name;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String inviteCode;
    private Long leaderId;
    private List<GroupMemberResponse> members;

    public static GroupDetailResponse of(TravelGroup group, List<GroupMemberResponse> members) {
        return GroupDetailResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .startAt(group.getStartAt())
                .endAt(group.getEndAt())
                .inviteCode(group.getInviteCode())
                .leaderId(group.getLeader().getId())
                .members(members)
                .build();
    }
}
