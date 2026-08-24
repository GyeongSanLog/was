package yu.spring.gyeongsanlog.letter.dto;

import lombok.Builder;
import lombok.Getter;
import yu.spring.gyeongsanlog.letter.domain.Letter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LetterResponse {
    private Long letterId;
    private Long writerId;
    private String writerNickname;
    private LocalDateTime createdAt;
    private String content;

    public static LetterResponse from(Letter letter) {
        return LetterResponse.builder()
                .letterId(letter.getId())
                .writerId(letter.getSender().getId())
                .writerNickname(letter.getSender().getNickname())
                .createdAt(letter.getCreatedAt())
                .content(letter.getContent())
                .build();
    }
}
