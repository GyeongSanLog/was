package yu.spring.gyeongsanlog.clip.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class UploadClipRequest {
    private String comment;

    @NotNull
    private LocalDateTime capturedAt;
}
