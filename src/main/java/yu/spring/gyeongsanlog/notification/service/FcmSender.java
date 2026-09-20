package yu.spring.gyeongsanlog.notification.service;

import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/*
 FCM 단건 발송 담당.
 서비스 계정 키가 없어 FirebaseMessaging 빈이 없을 수도 있으므로 ObjectProvider로 받아 없으면 조용히 건너뛴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FcmSender {

    public enum SendResult {
        SENT,
        // 토큰이 만료/삭제/형식오류라 재시도해도 소용없는 경우. 호출 측에서 토큰을 정리한다.
        INVALID_TOKEN,
        // 일시적 장애 등 다음 발송 때 다시 시도해볼 만한 실패
        FAILED,
        SKIPPED
    }

    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    public SendResult send(String token, String title, String body) {
        FirebaseMessaging messaging = firebaseMessagingProvider.getIfAvailable();
        if (messaging == null) {
            log.debug("Firebase가 비활성화되어 알림을 건너뜁니다.");
            return SendResult.SKIPPED;
        }

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            messaging.send(message);
            return SendResult.SENT;
        } catch (FirebaseMessagingException e) {
            if (isInvalidToken(e)) {
                log.info("무효한 FCM 토큰을 정리합니다. messagingErrorCode: {}", e.getMessagingErrorCode());
                return SendResult.INVALID_TOKEN;
            }
            log.error("FCM 발송 실패. errorCode: {}, messagingErrorCode: {}", e.getErrorCode(), e.getMessagingErrorCode(), e);
            return SendResult.FAILED;
        }
    }

    /*
     토큰이 만료됐으면 UNREGISTERED로 오지만, 형식 자체가 잘못된 토큰은 MessagingErrorCode가 null이고
     상위 ErrorCode만 INVALID_ARGUMENT로 온다. 이 경우도 재시도해봐야 의미가 없으므로 같이 정리 대상으로 본다.
     */
    private boolean isInvalidToken(FirebaseMessagingException e) {
        MessagingErrorCode messagingErrorCode = e.getMessagingErrorCode();
        if (messagingErrorCode == MessagingErrorCode.UNREGISTERED
                || messagingErrorCode == MessagingErrorCode.INVALID_ARGUMENT) {
            return true;
        }
        return messagingErrorCode == null && e.getErrorCode() == ErrorCode.INVALID_ARGUMENT;
    }
}
