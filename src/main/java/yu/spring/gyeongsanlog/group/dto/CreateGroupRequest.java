package yu.spring.gyeongsanlog.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreateGroupRequest {
    @NotBlank
    private String name;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;
}
