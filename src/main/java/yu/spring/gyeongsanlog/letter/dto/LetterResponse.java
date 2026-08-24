package yu.spring.gyeongsanlog.letter.dto;

import lombok.Builder;
import lombok.Getter;
import yu.spring.gyeongsanlog.letter.domain.Letter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LetterResponse {
    private Long id;
    private Long groupId;
    private Long senderId;
    private String senderNickname;
    private Long receiverId;
    private String content;
    private LocalDateTime createdAt;

    public static LetterResponse from(Letter letter) {
        return LetterResponse.builder()
                .id(letter.getId())
                .groupId(letter.getGroup().getId())
                .senderId(letter.getSender().getId())
                .senderNickname(letter.getSender().getNickname())
                .receiverId(letter.getReceiver().getId())
                .content(letter.getContent())
                .createdAt(letter.getCreatedAt())
                .build();
    }
}
