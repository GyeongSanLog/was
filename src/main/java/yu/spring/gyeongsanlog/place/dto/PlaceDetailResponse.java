package yu.spring.gyeongsanlog.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;
import yu.spring.gyeongsanlog.place.domain.Place;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "관광지 상세 정보")
public class PlaceDetailResponse {

    @Schema(description = "관광지 ID", example = "1")
    private Long id;

    @Schema(description = "관광지명", example = "반곡지")
    private String name;

    @Schema(description = "주소", example = "경상북도 경산시 남산면 반곡리 246")
    private String address;

    @Schema(description = "관광지 유형", example = "관광지")
    private String contentType;

    @Schema(description = "대표사진", example = "https://tong.visitkorea.or.kr/...")
    private String imageUrl;

    @Schema(description = "개요")
    private String overview;

    @Schema(description = "전화번호", example = "053-810-5364")
    private String tel;

    @Schema(description = "운영시간", example = "09:00~18:00")
    private String useTime;

    @Schema(description = "휴무일", example = "매주 월요일")
    private String restDate;

    @Schema(description = "주차 여부", example = "가능")
    private String parking;

    @Schema(description = "이용요금", example = "무료")
    private String useFee;

    @Schema(description = "홈페이지", example = "https://www.gbgs.go.kr/tour/")
    private String homepage;

    @Schema(description = "위도", example = "35.7804705930")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "128.8066359111")
    private BigDecimal longitude;

    public static PlaceDetailResponse from(Place place) {
        return PlaceDetailResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .address(toAddress(place))
                .contentType(place.getContentType().getLabel())
                .imageUrl(place.getImageUrl())
                .overview(place.getOverview())
                .tel(place.getTel())
                .useTime(place.getUseTime())
                .restDate(place.getRestDate())
                .parking(place.getParking())
                .useFee(place.getUseFee())
                .homepage(place.getHomepage())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .build();
    }

    private static String toAddress(Place place) {
        if (!StringUtils.hasText(place.getAddr2())) {
            return place.getAddr1();
        }
        return place.getAddr1() + " " + place.getAddr2();
    }
}
