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
 경산시 중심 관광지(연결이 가장 많은 곳) TOP5를 받아와 Redis에 캐싱한다.
 hubTatsCd가 place.contentId와 다른 코드 체계라 우리 관광지와 매칭하지 않고 TourAPI 원본만 보여준다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PopularPlaceSyncService {

    // 경산역/하양역처럼 기차역도 순위에 섞여 나와 3개만 뽑으면 역이 대부분을 차지할 수 있어 5개로 늘린다
    private static final int TOP_N = 5;
    // TourAPI 쪽 갱신이 몇 달씩 밀리는 경우가 있어(매월 8일 갱신 공지와 별개로), 데이터가 있는 가장 최근 달을 찾을 때까지 거슬러 올라간다
    private static final int MAX_LOOKBACK_MONTHS = 6;
    private static final DateTimeFormatter BASE_YM_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");

    private final TourApiClient tourApiClient;
    private final PopularPlaceCacheRepository popularPlaceCacheRepository;

    public void syncTop5() {
        LocalDate now = LocalDate.now();
        List<PopularPlaceItem> items = List.of();
        String triedBaseYm = null;

        for (int monthsBack = 0; monthsBack <= MAX_LOOKBACK_MONTHS && items.isEmpty(); monthsBack++) {
            triedBaseYm = now.minusMonths(monthsBack).format(BASE_YM_FORMAT);
            items = fetchWithFallback(triedBaseYm);
        }

        if (items.isEmpty()) {
            log.warn("중심 관광지 동기화 - 최근 {}개월 모두 데이터가 없습니다 (마지막 시도 baseYm={}).",
                    MAX_LOOKBACK_MONTHS + 1, triedBaseYm);
            return;
        }

        List<PopularPlaceResponse> top5 = items.stream()
                .sorted(Comparator.comparing(item -> parseRank(item.getHubRank())))
                .limit(TOP_N)
                .map(PopularPlaceResponse::from)
                .toList();

        popularPlaceCacheRepository.save(top5);
        log.info("중심 관광지 동기화 완료 - {}건 캐싱", top5.size());
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
