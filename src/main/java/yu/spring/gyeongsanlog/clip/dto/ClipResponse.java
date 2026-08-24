package yu.spring.gyeongsanlog.clip.dto;

import lombok.Builder;
import lombok.Getter;
import yu.spring.gyeongsanlog.clip.domain.Clip;

import java.time.LocalDateTime;

@Getter
@Builder
public class ClipResponse {
    private Long id;
    private Long groupId;
    private Long userId;
    private String nickname;
    private String videoUrl;
    private String comment;
    private Integer slotIndex;
    private LocalDateTime capturedAt;

    public static ClipResponse from(Clip clip) {
        return ClipResponse.builder()
                .id(clip.getId())
                .groupId(clip.getGroup().getId())
                .userId(clip.getUser().getId())
                .nickname(clip.getUser().getNickname())
                .videoUrl(clip.getVideoUrl())
                .comment(clip.getComment())
                .slotIndex(clip.getSlotIndex())
                .capturedAt(clip.getCapturedAt())
                .build();
    }
}
