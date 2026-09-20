package yu.spring.gyeongsanlog.letter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WriteLetterRequest {
    @NotNull
    private Long receiverId;

    @NotBlank
    private String content;
}
