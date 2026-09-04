package yu.spring.gyeongsanlog.place.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yu.spring.gyeongsanlog.place.service.PopularPlaceSyncService;

/*
 중심 관광지 정보는 매월 8일에만 갱신되므로 다음날인 매월 9일에 한 번만 동기화한다.
 주기는 tour-api.popular-sync-cron으로 조정한다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PopularPlaceSyncScheduler {

    private final PopularPlaceSyncService popularPlaceSyncService;

    @Scheduled(cron = "${tour-api.popular-sync-cron}", zone = "Asia/Seoul")
    public void syncPopularPlaces() {
        log.info("중심 관광지 동기화 스케줄러 시작");
        try {
            popularPlaceSyncService.syncTop3();
        } catch (Exception e) {
            log.error("중심 관광지 동기화 스케줄러 실패", e);
        }
    }
}
