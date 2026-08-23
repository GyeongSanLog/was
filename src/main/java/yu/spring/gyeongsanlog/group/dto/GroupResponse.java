package yu.spring.gyeongsanlog.group.dto;

import lombok.Builder;
import lombok.Getter;
import yu.spring.gyeongsanlog.group.domain.TravelGroup;

import java.time.LocalDateTime;

@Getter
@Builder
public class GroupResponse {
    private Long id;
    private String name;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String inviteCode;
    private Long leaderId;

    public static GroupResponse from(TravelGroup group) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .startAt(group.getStartAt())
                .endAt(group.getEndAt())
                .inviteCode(group.getInviteCode())
                .leaderId(group.getLeader().getId())
                .build();
    }
}
