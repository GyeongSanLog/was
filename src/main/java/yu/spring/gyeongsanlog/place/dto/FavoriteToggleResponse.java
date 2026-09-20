package yu.spring.gyeongsanlog.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "찜 토글 결과")
public class FavoriteToggleResponse {

    @Schema(description = "토글 후 찜 상태. true면 찜됨, false면 찜 취소됨", example = "true")
    private boolean favorited;
}
