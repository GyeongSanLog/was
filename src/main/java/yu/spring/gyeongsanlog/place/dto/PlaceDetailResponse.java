package yu.spring.gyeongsanlog.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;
import yu.spring.gyeongsanlog.place.domain.Place;
import yu.spring.gyeongsanlog.place.domain.PlaceImage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    private String category;

    @Schema(description = "대표사진", example = "https://tong.visitkorea.or.kr/...")
    private String imageUrl;

    @Schema(description = "추가 사진 목록. 없으면 빈 배열")
    private List<String> imageUrls;

    @Schema(description = "장소 설명")
    private String content;

    @Schema(description = "전화번호", example = "053-810-5364")
    private String phoneNumber;

    @Schema(description = "운영시간", example = "09:00~18:00")
    private String useTime;

    @Schema(description = "휴무일", example = "매주 월요일")
    private String restDate;

    @Schema(description = "주차 여부", example = "가능")
    private String parking;

    @Schema(description = "축제 시작일. 축제가 아니면 null", example = "2026-09-19")
    private LocalDate eventStartDate;

    @Schema(description = "축제 종료일. 축제가 아니면 null", example = "2026-09-20")
    private LocalDate eventEndDate;

    @Schema(description = "엘리베이터 여부. 정보 없으면 null")
    private String elevator;

    @Schema(description = "화장실 여부. 정보 없으면 null")
    private String restroom;

    @Schema(description = "유모차 대여 여부. 정보 없으면 null")
    private String stroller;

    @Schema(description = "홈페이지", example = "https://www.gbgs.go.kr/tour/")
    private String homepage;

    @Schema(description = "위도", example = "35.7804705930")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "128.8066359111")
    private BigDecimal longitude;

    public static PlaceDetailResponse from(Place place, List<PlaceImage> images) {
        return PlaceDetailResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .address(toAddress(place))
                .category(place.getContentType().getLabel())
                .imageUrl(place.getImageUrl())
                .imageUrls(images.stream().map(PlaceImage::getOriginUrl).toList())
                .content(place.getOverview())
                .phoneNumber(place.getTel())
                .useTime(place.getUseTime())
                .restDate(place.getRestDate())
                .parking(place.getParking())
                .eventStartDate(place.getEventStartDate())
                .eventEndDate(place.getEventEndDate())
                .elevator(place.getElevator())
                .restroom(place.getRestroom())
                .stroller(place.getStroller())
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
