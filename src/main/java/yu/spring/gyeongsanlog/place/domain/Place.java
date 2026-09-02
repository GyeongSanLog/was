package yu.spring.gyeongsanlog.place.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yu.spring.gyeongsanlog.common.domain.BaseTimeEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 기본 정보는 areaBasedList2, 설명/홈페이지는 detailCommon2, 전화번호/편의시설은 detailIntro2에서 가져온다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "place", indexes = {
        @Index(name = "idx_place_coord", columnList = "latitude, longitude"),
        @Index(name = "idx_place_content_type", columnList = "content_type")
})
public class Place extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String contentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private ContentType contentType;

    @Column(nullable = false, length = 200)
    private String name;

    private String addr1; // 시/군/구 까지만

    private String addr2; // 상세주소

    @Column(nullable = false, precision = 13, scale = 10)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 13, scale = 10)
    private BigDecimal longitude;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String overview; // 개요

    @Column(length = 500)
    private String homepage;

    @Column(length = 100)
    private String tel;

    @Column(columnDefinition = "TEXT")
    private String useTime;  // 운영시간

    @Column(columnDefinition = "TEXT")
    private String restDate; // 휴무일

    private String parking; // 주차여부

    private String useFee;

    private LocalDate eventStartDate; // 축제 전용

    private LocalDate eventEndDate; // 축제 전용

    @Column(columnDefinition = "TEXT")
    private String elevator;

    @Column(columnDefinition = "TEXT")
    private String restroom;

    @Column(columnDefinition = "TEXT")
    private String stroller; // 유모차 대여 여부

    @Column(length = 10)
    private String ldongRegnCd; // TourAPI 지역 식별 코드(도)

    @Column(length = 10)
    private String ldongSignguCd; // TourAPI 지역 식별 코드(시)

    //TourAPI modifiedtime. 재동기화 시 변경 여부 판단에 쓴다.
    private LocalDateTime apiModifiedAt;

    // 상세정보(개요/전화번호/편의시설)를 마지막으로 받아온 시각.
    // apiModifiedAt보다 이르면 관광지 정보가 그 뒤에 바뀐 것이므로 다시 받는다.
    private LocalDateTime detailSyncedAt;

    @Builder
    public Place(String contentId, ContentType contentType, String name, String addr1, String addr2,
                 BigDecimal latitude, BigDecimal longitude, String imageUrl, String thumbnailUrl,
                 String ldongRegnCd, String ldongSignguCd, LocalDateTime apiModifiedAt) {
        this.contentId = contentId;
        this.contentType = contentType;
        this.name = name;
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.ldongRegnCd = ldongRegnCd;
        this.ldongSignguCd = ldongSignguCd;
        this.apiModifiedAt = apiModifiedAt;
    }

    // areaBasedList2 재동기화
    public void updateBasicInfo(ContentType contentType, String name, String addr1, String addr2,
                                BigDecimal latitude, BigDecimal longitude, String imageUrl, String thumbnailUrl,
                                LocalDateTime apiModifiedAt) {
        this.contentType = contentType;
        this.name = name;
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.apiModifiedAt = apiModifiedAt;
    }

    // detailCommon2
    public void updateOverview(String overview, String homepage) {
        this.overview = overview;
        this.homepage = homepage;
    }

    // detailIntro2
    public void updateIntro(String tel, String useTime, String restDate, String parking, String useFee,
                            LocalDate eventStartDate, LocalDate eventEndDate) {
        this.tel = tel;
        this.useTime = useTime;
        this.restDate = restDate;
        this.parking = parking;
        this.useFee = useFee;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
    }

    // detailWithTour2
    public void updateAccessibility(String elevator, String restroom, String stroller) {
        this.elevator = elevator;
        this.restroom = restroom;
        this.stroller = stroller;
    }

    public void markDetailSynced(LocalDateTime syncedAt) {
        this.detailSyncedAt = syncedAt;
    }

    /** 상세정보를 받은 적이 없거나, 받은 뒤에 관광지 정보가 바뀌었으면 다시 받아야 한다 */
    public boolean needsDetailSync() {
        if (detailSyncedAt == null) {
            return true;
        }
        return apiModifiedAt != null && apiModifiedAt.isAfter(detailSyncedAt);
    }
}
