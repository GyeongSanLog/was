package yu.spring.gyeongsanlog.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관광지 상세정보 동기화 결과")
public class PlaceDetailSyncResult {

    @Schema(description = "상세정보를 받아야 하는 대상 건수", example = "110")
    private int targeted;

    @Schema(description = "실제로 갱신한 건수", example = "110")
    private int updated;

    @Schema(description = "호출 실패로 건너뛴 건수", example = "0")
    private int failed;
}
