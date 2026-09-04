package yu.spring.gyeongsanlog.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yu.spring.gyeongsanlog.place.config.TourApiClient;
import yu.spring.gyeongsanlog.place.config.dto.PopularPlaceItem;
import yu.spring.gyeongsanlog.place.dto.PopularPlaceResponse;
import yu.spring.gyeongsanlog.place.repository.PopularPlaceCacheRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/*
 경산시 중심 관광지(연결이 가장 많은 곳) TOP3를 받아와 Redis에 캐싱한다.
 hubTatsCd가 place.contentId와 다른 코드 체계라 우리 관광지와 매칭하지 않고 TourAPI 원본만 보여준다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PopularPlaceSyncService {

    private static final int TOP_N = 3;
    private static final DateTimeFormatter BASE_YM_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");

    private final TourApiClient tourApiClient;
    private final PopularPlaceCacheRepository popularPlaceCacheRepository;

    public void syncTop3() {
        LocalDate now = LocalDate.now();
        List<PopularPlaceItem> items = fetchWithFallback(now.format(BASE_YM_FORMAT));

        if (items.isEmpty()) {
            // 이번 달 데이터가 아직 갱신 전(매월 8일)일 수 있어 지난달로 한 번 더 시도한다
            items = fetchWithFallback(now.minusMonths(1).format(BASE_YM_FORMAT));
        }

        if (items.isEmpty()) {
            log.warn("중심 관광지 동기화 - 이번 달/지난달 모두 데이터가 없습니다.");
            return;
        }

        List<PopularPlaceResponse> top3 = items.stream()
                .sorted(Comparator.comparing(item -> parseRank(item.getHubRank())))
                .limit(TOP_N)
                .map(PopularPlaceResponse::from)
                .toList();

        popularPlaceCacheRepository.save(top3);
        log.info("중심 관광지 동기화 완료 - {}건 캐싱", top3.size());
    }

    private List<PopularPlaceItem> fetchWithFallback(String baseYm) {
        try {
            return tourApiClient.fetchPopularPlaces(baseYm);
        } catch (Exception e) {
            log.warn("중심 관광지 동기화 실패 - baseYm={}", baseYm, e);
            return List.of();
        }
    }

    private int parseRank(String hubRank) {
        try {
            return Integer.parseInt(hubRank.trim());
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }
}
