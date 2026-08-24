package yu.spring.gyeongsanlog.letter.dto;

import lombok.Builder;
import lombok.Getter;
import yu.spring.gyeongsanlog.letter.domain.Letter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LetterListResponse {
    private Long letterId;
    private String writerNickname;
    private LocalDateTime createdAt;

    public static LetterListResponse from(Letter letter) {
        return LetterListResponse.builder()
                .letterId(letter.getId())
                .writerNickname(letter.getSender().getNickname())
                .createdAt(letter.getCreatedAt())
                .build();
    }
}
