package yu.spring.gyeongsanlog.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;
import yu.spring.gyeongsanlog.place.domain.Place;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "관광지 목록 항목")
public class
PlaceListResponse {

    @Schema(description = "관광지 ID", example = "1")
    private Long id;

    @Schema(description = "관광지명", example = "반곡지")
    private String name;

    @Schema(description = "주소", example = "경상북도 경산시 남산면 반곡리 246")
    private String address;

    @Schema(description = "대표사진. 없으면 null", example = "https://tong.visitkorea.or.kr/...")
    private String imageUrl;

    @Schema(description = "관광지 유형", example = "관광지")
    private String category;

    @Schema(description = "축제 시작일. 축제가 아니거나 정보가 없으면 null", example = "2026-09-19")
    private LocalDate eventStartDate;

    @Schema(description = "축제 종료일. 축제가 아니거나 정보가 없으면 null", example = "2026-09-20")
    private LocalDate eventEndDate;

    public static PlaceListResponse from(Place place) {
        return PlaceListResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .address(toAddress(place))
                .imageUrl(toThumbnail(place))
                .category(place.getContentType().getLabel())
                .eventStartDate(place.getEventStartDate())
                .eventEndDate(place.getEventEndDate())
                .build();
    }

    private static String toAddress(Place place) {
        if (!StringUtils.hasText(place.getAddr2())) {
            return place.getAddr1();
        }
        return place.getAddr1() + " " + place.getAddr2();
    }

    /** 목록은 썸네일이면 충분하지만, 썸네일이 없는 곳이 있어 원본으로 대체한다 */
    private static String toThumbnail(Place place) {
        return StringUtils.hasText(place.getThumbnailUrl()) ? place.getThumbnailUrl() : place.getImageUrl();
    }
}
