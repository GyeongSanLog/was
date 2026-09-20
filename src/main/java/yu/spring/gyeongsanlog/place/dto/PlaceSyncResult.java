package yu.spring.gyeongsanlog.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관광지 동기화 결과")
public class PlaceSyncResult {

    @Schema(description = "TourAPI에서 받은 건수", example = "110")
    private int fetched;

    @Schema(description = "새로 저장한 건수", example = "110")
    private int created;

    @Schema(description = "변경되어 갱신한 건수", example = "0")
    private int updated;

    @Schema(description = "변경 없어 건너뛴 건수", example = "0")
    private int unchanged;

    @Schema(description = "좌표나 타입이 없어 제외한 건수", example = "0")
    private int skipped;
}
