package yu.spring.gyeongsanlog.place.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yu.spring.gyeongsanlog.place.dto.PlaceDetailSyncResult;
import yu.spring.gyeongsanlog.place.dto.PlaceSyncResult;
import yu.spring.gyeongsanlog.place.service.PlaceSyncService;

/*
 TourAPI 관광정보는 일 1회 갱신되므로 매일 새벽에 한 번만 동기화한다.
 주기는 tour-api.sync-cron으로 조정한다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PlaceSyncScheduler {

    private final PlaceSyncService placeSyncService;

    @Scheduled(cron = "${tour-api.sync-cron}", zone = "Asia/Seoul")
    public void syncPlaces() {
        log.info("관광지 동기화 스케줄러 시작");
        try {
            // 기본 정보를 먼저 반영해야 apiModifiedAt이 최신이 되고,
            // 그 값을 기준으로 상세정보를 다시 받을 대상이 결정된다.
            PlaceSyncResult basic = placeSyncService.syncBasicInfo();
            PlaceDetailSyncResult detail = placeSyncService.syncDetails();

            log.info("관광지 동기화 스케줄러 종료 - 기본(신규 {}건, 갱신 {}건), 상세(갱신 {}건, 실패 {}건)",
                    basic.getCreated(), basic.getUpdated(), detail.getUpdated(), detail.getFailed());
        } catch (Exception e) {
            // 여기서 예외가 새어나가면 다음 실행까지 원인을 알 수 없으므로 반드시 남긴다
            log.error("관광지 동기화 스케줄러 실패", e);
        }
    }
}
