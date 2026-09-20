package yu.spring.gyeongsanlog.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yu.spring.gyeongsanlog.place.config.dto.PopularPlaceItem;

// Redis에 캐싱했다가 다시 읽어올 때 Jackson이 역직렬화해야 해서 기본/전체 생성자가 필요하다
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "중심 관광지 순위 항목. TourAPI 원본을 그대로 보여주며 자체 관광지(place)와는 연결하지 않는다")
public class PopularPlaceResponse {

    @Schema(description = "관광지명", example = "반곡지")
    private String name;

    @Schema(description = "카테고리 대분류", example = "관광지")
    private String categoryLarge;

    @Schema(description = "카테고리 중분류", example = "자연관광")
    private String categoryMedium;

    @Schema(description = "위도")
    private Double latitude;

    @Schema(description = "경도")
    private Double longitude;

    @Schema(description = "중심지 순위 (1이 가장 많이 연결됨)", example = "1")
    private Integer rank;

    public static PopularPlaceResponse from(PopularPlaceItem item) {
        return PopularPlaceResponse.builder()
                .name(item.getHubTatsNm())
                .categoryLarge(item.getHubCtgryLclsNm())
                .categoryMedium(item.getHubCtgryMclsNm())
                .latitude(toDouble(item.getMapY()))
                .longitude(toDouble(item.getMapX()))
                .rank(toInt(item.getHubRank()))
                .build();
    }

    private static Double toDouble(String value) {
        try {
            return value == null ? null : Double.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer toInt(String value) {
        try {
            return value == null ? null : Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
