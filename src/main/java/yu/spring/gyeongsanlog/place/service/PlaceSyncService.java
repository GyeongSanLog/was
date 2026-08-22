package yu.spring.gyeongsanlog.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yu.spring.gyeongsanlog.place.domain.ContentType;
import yu.spring.gyeongsanlog.place.domain.Place;
import yu.spring.gyeongsanlog.place.dto.PlaceDetailSyncResult;
import yu.spring.gyeongsanlog.place.dto.PlaceSyncResult;
import yu.spring.gyeongsanlog.place.config.TourApiClient;
import yu.spring.gyeongsanlog.place.config.TourApiTextCleaner;
import yu.spring.gyeongsanlog.place.config.dto.AreaBasedItem;
import yu.spring.gyeongsanlog.place.config.dto.DetailCommonItem;
import yu.spring.gyeongsanlog.place.config.dto.DetailImageItem;
import yu.spring.gyeongsanlog.place.repository.PlaceImageRepository;
import yu.spring.gyeongsanlog.place.repository.PlaceRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/*
 TourAPI의 관광지 정보를 place 테이블에 반영한다.
 기본 정보(areaBasedList2)는 1회 호출로 전체를 받지만,
 상세 정보(detailCommon2/detailIntro2)는 관광지마다 호출해야 해서 나눠 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceSyncService {

    private final TourApiClient tourApiClient;
    private final PlaceRepository placeRepository;
    private final PlaceImageSyncService placeImageSyncService;
    private final PlaceImageRepository placeImageRepository;

    @Transactional
    public PlaceSyncResult syncBasicInfo() {
        List<AreaBasedItem> items = tourApiClient.fetchPlaces();

        int created = 0;
        int updated = 0;
        int unchanged = 0;
        int skipped = 0;

        for (AreaBasedItem item : items) {
            ContentType contentType = resolveContentType(item);

            if (contentType == null || !item.hasCoordinates()) {
                log.warn("동기화 제외 - contentId={}, title={} (타입={}, 좌표={}/{})",
                        item.getContentid(), item.getTitle(), item.getContenttypeid(),
                        item.getMapy(), item.getMapx());
                skipped++;
                continue;
            }

            Optional<Place> found = placeRepository.findByContentId(item.getContentid());

            if (found.isEmpty()) {
                placeRepository.save(toPlace(item, contentType));
                created++;
                continue;
            }

            Place place = found.get();
            if (isUpToDate(place, item.getModifiedAt())) {
                unchanged++;
                continue;
            }

            place.updateBasicInfo(
                    contentType,
                    item.getTitle(),
                    item.getAddr1(),
                    item.getAddr2(),
                    item.getLatitude(),
                    item.getLongitude(),
                    item.getFirstimage(),
                    item.getFirstimage2(),
                    item.getLclsSystm1(),
                    item.getLclsSystm2(),
                    item.getLclsSystm3(),
                    item.getModifiedAt()
            );
            updated++;
        }

        log.info("관광지 기본 정보 동기화 완료 - 수신 {}건, 신규 {}건, 갱신 {}건, 변경없음 {}건, 제외 {}건",
                items.size(), created, updated, unchanged, skipped);

        return PlaceSyncResult.builder()
                .fetched(items.size())
                .created(created)
                .updated(updated)
                .unchanged(unchanged)
                .skipped(skipped)
                .build();
    }

    /*
     상세정보는 관광지 1곳당 3회(detailCommon2 + detailIntro2 + detailImage2)를 호출한다.
     외부 호출이 길어지므로 트랜잭션을 걸지 않고, 조회로 분리(detached)된 엔티티를 수정한 뒤
     마지막에 saveAll로 한 번에 반영한다. 네트워크 대기 동안 DB 커넥션을 잡고 있지 않기 위해서다.
     사진은 place와 별도 테이블이라 applyDetail 안에서 PlaceImageSyncService를 통해 그때그때 반영한다.
     */
    public PlaceDetailSyncResult syncDetails() {
        List<Place> targets = placeRepository.findAll().stream()
                .filter(Place::needsDetailSync)
                .toList();

        if (targets.isEmpty()) {
            log.info("상세정보 동기화 대상이 없습니다.");
            return PlaceDetailSyncResult.builder().build();
        }

        log.info("관광지 상세정보 동기화 시작 - 대상 {}건 (API 호출 약 {}회)", targets.size(), targets.size() * 3);

        LocalDateTime syncedAt = LocalDateTime.now();
        List<Place> changed = new ArrayList<>();
        int failed = 0;

        for (Place place : targets) {
            try {
                applyDetail(place, syncedAt);
                changed.add(place);
            } catch (Exception e) {
                log.warn("상세정보 동기화 실패 - contentId={}, name={}",
                        place.getContentId(), place.getName(), e);
                failed++;
            }
        }

        placeRepository.saveAll(changed);

        log.info("관광지 상세정보 동기화 완료 - 대상 {}건, 갱신 {}건, 실패 {}건",
                targets.size(), changed.size(), failed);

        return PlaceDetailSyncResult.builder()
                .targeted(targets.size())
                .updated(changed.size())
                .failed(failed)
                .build();
    }

    /*
     사진은 이번에 새로 추가된 항목이라, 이미 상세정보를 동기화해서 needsDetailSync()가
     false인 기존 관광지들은 syncDetails()로 못 채운다. 사진이 하나도 없는 곳만 골라
     detailImage2만 호출한다 (관광지당 1회, common/intro는 다시 부르지 않는다).
     */
    public PlaceDetailSyncResult syncMissingImages() {
        Set<Long> withImages = new HashSet<>(placeImageRepository.findAllPlaceIdsWithImages());
        List<Place> targets = placeRepository.findAll().stream()
                .filter(place -> !withImages.contains(place.getId()))
                .toList();

        if (targets.isEmpty()) {
            log.info("사진 동기화 대상이 없습니다.");
            return PlaceDetailSyncResult.builder().build();
        }

        log.info("관광지 사진 동기화 시작 - 대상 {}건 (API 호출 약 {}회)", targets.size(), targets.size());

        int updated = 0;
        int failed = 0;

        for (Place place : targets) {
            try {
                List<DetailImageItem> images = tourApiClient.fetchDetailImages(place.getContentId());
                placeImageSyncService.replaceImages(place.getId(), images);
                updated++;
            } catch (Exception e) {
                log.warn("사진 동기화 실패 - contentId={}, name={}", place.getContentId(), place.getName(), e);
                failed++;
            }
        }

        log.info("관광지 사진 동기화 완료 - 대상 {}건, 갱신 {}건, 실패 {}건", targets.size(), updated, failed);

        return PlaceDetailSyncResult.builder()
                .targeted(targets.size())
                .updated(updated)
                .failed(failed)
                .build();
    }

    private void applyDetail(Place place, LocalDateTime syncedAt) {
        DetailCommonItem common = tourApiClient.fetchDetailCommon(place.getContentId());
        if (common != null) {
            place.updateOverview(
                    TourApiTextCleaner.clean(common.getOverview()),
                    TourApiTextCleaner.clean(common.getHomepage())
            );
        }

        ContentType type = place.getContentType();
        Map<String, String> intro = tourApiClient.fetchDetailIntro(place.getContentId(), type.getCode());

        place.updateIntro(
                pick(intro, type.getTelField()),
                pick(intro, type.getUseTimeField()),
                pick(intro, type.getRestDateField()),
                pick(intro, type.getParkingField()),
                pick(intro, type.getUseFeeField())
        );

        List<DetailImageItem> images = tourApiClient.fetchDetailImages(place.getContentId());
        placeImageSyncService.replaceImages(place.getId(), images);

        place.markDetailSynced(syncedAt);
    }

    /** 해당 타입에 없는 항목은 필드명이 null이라 조회하지 않는다 (예: 축제는 휴무일 필드가 없다) */
    private String pick(Map<String, String> intro, String field) {
        return field == null ? null : TourApiTextCleaner.clean(intro.get(field));
    }

    private ContentType resolveContentType(AreaBasedItem item) {
        try {
            return ContentType.from(item.getContenttypeid());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // 새롭지 않으면 갱신 x
    private boolean isUpToDate(Place place, LocalDateTime apiModifiedAt) {
        LocalDateTime saved = place.getApiModifiedAt();
        return saved != null && apiModifiedAt != null && !apiModifiedAt.isAfter(saved);
    }

    private Place toPlace(AreaBasedItem item, ContentType contentType) {
        return Place.builder()
                .contentId(item.getContentid())
                .contentType(contentType)
                .name(item.getTitle())
                .addr1(item.getAddr1())
                .addr2(item.getAddr2())
                .latitude(item.getLatitude())
                .longitude(item.getLongitude())
                .imageUrl(item.getFirstimage())
                .thumbnailUrl(item.getFirstimage2())
                .lclsSystm1(item.getLclsSystm1())
                .lclsSystm2(item.getLclsSystm2())
                .lclsSystm3(item.getLclsSystm3())
                .ldongRegnCd(item.getLdongRegnCd())
                .ldongSignguCd(item.getLdongSignguCd())
                .apiModifiedAt(item.getModifiedAt())
                .build();
    }
}
