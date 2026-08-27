package yu.spring.gyeongsanlog.common.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yu.spring.gyeongsanlog.user.domain.Provider;
import yu.spring.gyeongsanlog.user.domain.User;
import yu.spring.gyeongsanlog.user.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyDataInitializer implements CommandLineRunner {

    private static final String DUMMY_EMAIL = "test@test.com";
    private static final String DUMMY_PASSWORD = "test1234";
    private static final String DUMMY_NICKNAME = "test";
    private static final String DUMMY_NAME = "테스트";

    // 공모전 제출 지정 형식의 심사용 계정
    private static final String OPENAPI_EMAIL = "openapi@메일도메인";
    private static final String OPENAPI_PASSWORD = "2026openapi!";
    private static final String OPENAPI_NICKNAME = "openapi";
    private static final String OPENAPI_NAME = "심사용계정";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        createIfAbsent(DUMMY_EMAIL, DUMMY_PASSWORD, DUMMY_NICKNAME, DUMMY_NAME);
        createIfAbsent(OPENAPI_EMAIL, OPENAPI_PASSWORD, OPENAPI_NICKNAME, OPENAPI_NAME);
    }

    private void createIfAbsent(String email, String password, String nickname, String name) {
        if (userRepository.existsByEmailAndProvider(email, Provider.LOCAL)) {
            log.info("초기 유저가 이미 존재합니다. 건너뜁니다. (email: {})", email);
            return;
        }

        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .name(name)
                .password(passwordEncoder.encode(password))
                .provider(Provider.LOCAL)
                .build();

        userRepository.save(user);

        log.info("초기 유저 생성 완료 (email: {}, password: {})", email, password);
    }
}
