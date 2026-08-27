package yu.spring.gyeongsanlog.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yu.spring.gyeongsanlog.group.domain.GroupMember;
import yu.spring.gyeongsanlog.group.domain.TravelGroup;
import yu.spring.gyeongsanlog.group.repository.GroupMemberRepository;
import yu.spring.gyeongsanlog.group.repository.TravelGroupRepository;
import yu.spring.gyeongsanlog.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;

/*
 진행 중인 그룹의 멤버들에게 "지금 찍을 시간" 알림을 보낸다.
 슬롯이 막 시작된 시점이라 아직 아무도 촬영하지 않았으므로 그룹 전원에게 발송한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClipReminderService {

    private static final String TITLE = "셋로그";
    private static final String BODY = "지금 이 순간을 찍어보세요!";

    private final TravelGroupRepository travelGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final FcmSender fcmSender;

    @Transactional
    public void notifyOngoingGroups() {
        LocalDateTime now = LocalDateTime.now();
        List<TravelGroup> ongoing = travelGroupRepository.findByStartAtBeforeAndEndAtAfter(now, now);
        if (ongoing.isEmpty()) {
            return;
        }

        int sent = 0;
        int failed = 0;
        int cleaned = 0;
        for (TravelGroup group : ongoing) {
            for (GroupMember member : groupMemberRepository.findByGroupIdWithUser(group.getId())) {
                User user = member.getUser();
                String token = user.getFcmToken();
                if (token == null || token.isBlank()) {
                    continue;
                }
                switch (fcmSender.send(token, TITLE, BODY)) {
                    case SENT -> sent++;
                    case INVALID_TOKEN -> {
                        // 더 이상 유효하지 않은 토큰은 지워서 다음 발송 때 시도하지 않는다
                        user.updateFcmToken(null);
                        cleaned++;
                    }
                    case FAILED -> failed++;
                    case SKIPPED -> { }
                }
            }
        }
        log.info("촬영 알림 처리 완료. 대상 그룹 {}건, 발송 {}건, 실패 {}건, 토큰정리 {}건",
                ongoing.size(), sent, failed, cleaned);
    }
}
