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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmailAndProvider(DUMMY_EMAIL, Provider.LOCAL)) {
            log.info("더미 유저가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        User user = User.builder()
                .email(DUMMY_EMAIL)
                .nickname(DUMMY_NICKNAME)
                .password(passwordEncoder.encode(DUMMY_PASSWORD))
                .provider(Provider.LOCAL)
                .build();

        userRepository.save(user);

        log.info("더미 유저 생성 완료 (email: {}, password: {})", DUMMY_EMAIL, DUMMY_PASSWORD);
    }
}
