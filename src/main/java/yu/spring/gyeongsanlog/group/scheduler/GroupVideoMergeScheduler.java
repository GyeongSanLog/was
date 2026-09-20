package yu.spring.gyeongsanlog.group.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yu.spring.gyeongsanlog.group.domain.MergeStatus;
import yu.spring.gyeongsanlog.group.domain.TravelGroup;
import yu.spring.gyeongsanlog.group.repository.TravelGroupRepository;
import yu.spring.gyeongsanlog.group.service.GroupVideoMergeService;

import java.time.LocalDateTime;
import java.util.List;

// 그룹 endAt이 지났는데 아직 병합되지 않은 그룹을 찾아 영상 병합을 트리거한다.
@Component
@Slf4j
@RequiredArgsConstructor
public class GroupVideoMergeScheduler {

    private final TravelGroupRepository travelGroupRepository;
    private final GroupVideoMergeService groupVideoMergeService;

    @Scheduled(cron = "${merge.scan-cron}", zone = "Asia/Seoul")
    public void mergeEndedGroups() {
        List<TravelGroup> targets = travelGroupRepository.findByEndAtBeforeAndMergeStatus(
                LocalDateTime.now(), MergeStatus.NOT_STARTED);

        if (targets.isEmpty()) {
            return;
        }

        log.info("영상 병합 대상 그룹 {}건 발견", targets.size());
        for (TravelGroup group : targets) {
            groupVideoMergeService.mergeGroupVideo(group.getId());
        }
    }
}
