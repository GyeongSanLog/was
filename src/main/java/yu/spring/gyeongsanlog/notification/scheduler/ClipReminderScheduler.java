package yu.spring.gyeongsanlog.notification.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yu.spring.gyeongsanlog.notification.service.ClipReminderService;

// 매시 정각에 진행 중인 그룹 멤버들에게 촬영 알림을 보낸다.
@Slf4j
@Component
@RequiredArgsConstructor
public class ClipReminderScheduler {

    private final ClipReminderService clipReminderService;

    @Scheduled(cron = "${notification.clip-reminder-cron}", zone = "Asia/Seoul")
    public void sendClipReminders() {
        try {
            clipReminderService.notifyOngoingGroups();
        } catch (Exception e) {
            // 여기서 예외가 새어나가면 다음 실행까지 원인을 알 수 없으므로 반드시 남긴다
            log.error("촬영 알림 스케줄러 실패", e);
        }
    }
}
