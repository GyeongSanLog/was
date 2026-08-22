package yu.spring.gyeongsanlog.common.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import yu.spring.gyeongsanlog.place.dto.PlaceDetailSyncResult;
import yu.spring.gyeongsanlog.place.dto.PlaceSyncResult;
import yu.spring.gyeongsanlog.place.repository.PlaceRepository;
import yu.spring.gyeongsanlog.place.service.PlaceSyncService;

/*
 관광지 테이블이 비어 있을 때만 TourAPI에서 초기 적재한다.
 데이터가 이미 있으면 호출하지 않으므로 재시작이 잦아도 API 사용량에 영향이 없다.
 이후 갱신은 PlaceSyncScheduler가 맡는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceDataInitializer implements CommandLineRunner {

    private final PlaceRepository placeRepository;
    private final PlaceSyncService placeSyncService;

    @Override
    public void run(String... args) {
        long count = placeRepository.count();

        if (count > 0) {
            log.info("관광지 데이터가 이미 {}건 존재합니다. 초기 적재를 건너뜁니다.", count);
            return;
        }

        log.info("관광지 데이터가 비어 있어 초기 적재를 시작합니다.");
        try {
            PlaceSyncResult basic = placeSyncService.syncBasicInfo();
            log.info("관광지 기본 정보 초기 적재 완료 - 신규 {}건, 제외 {}건",
                    basic.getCreated(), basic.getSkipped());

            // 관광지마다 2회씩 호출하므로 최초 1회는 1~2분 걸린다.
            // 웹 서버는 이미 떠 있는 상태라 요청 처리는 막지 않는다.
            PlaceDetailSyncResult detail = placeSyncService.syncDetails();
            log.info("관광지 상세정보 초기 적재 완료 - 갱신 {}건, 실패 {}건",
                    detail.getUpdated(), detail.getFailed());
        } catch (Exception e) {
            // 외부 API 장애로 앱 기동이 막히면 안 되므로 로그만 남긴다. 스케줄러가 다시 채운다.
            log.error("관광지 초기 적재 실패. 다음 스케줄러 실행 때 재시도됩니다.", e);
        }
    }
}
