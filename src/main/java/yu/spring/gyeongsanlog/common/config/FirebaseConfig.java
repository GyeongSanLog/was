package yu.spring.gyeongsanlog.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/*
 FCM 발송에 쓸 FirebaseApp을 초기화한다.
 서비스 계정 키가 없으면 앱 전체가 죽지 않도록 초기화만 건너뛰고, 발송 시점에 알림 기능만 비활성화된다.
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-path}")
    private String serviceAccountPath;

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        Resource resource = new FileSystemResource(serviceAccountPath);
        if (!resource.exists()) {
            log.warn("Firebase 서비스 계정 키를 찾을 수 없어 푸시 알림을 비활성화합니다. path: {}", serviceAccountPath);
            return null;
        }

        try (InputStream credentials = resource.getInputStream()) {
            FirebaseApp app = FirebaseApp.getApps().isEmpty()
                    ? FirebaseApp.initializeApp(FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build())
                    : FirebaseApp.getInstance();

            log.info("Firebase 초기화 완료");
            return FirebaseMessaging.getInstance(app);
        } catch (IOException e) {
            log.error("Firebase 초기화 실패. 푸시 알림이 비활성화됩니다.", e);
            return null;
        }
    }
}
