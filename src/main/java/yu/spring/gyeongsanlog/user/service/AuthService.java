package yu.spring.gyeongsanlog.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;
import yu.spring.gyeongsanlog.common.jwt.JwtTokenProvider;
import yu.spring.gyeongsanlog.common.jwt.RefreshTokenRepository;
import yu.spring.gyeongsanlog.user.domain.Provider;
import yu.spring.gyeongsanlog.user.domain.User;
import yu.spring.gyeongsanlog.user.dto.LoginRequest;
import yu.spring.gyeongsanlog.user.dto.RefreshRequest;
import yu.spring.gyeongsanlog.user.dto.SignUpRequest;
import yu.spring.gyeongsanlog.user.dto.TokenResponse;
import yu.spring.gyeongsanlog.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    //회원가입
    @Transactional
    public TokenResponse register(SignUpRequest request) {
        if (userRepository.existsByEmailAndProvider(request.getEmail(), Provider.LOCAL)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(Provider.LOCAL)
                .build();

        userRepository.save(user);

        return issueTokens(user);
    }

    // 시스템 로그인
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndProvider(request.getEmail(), Provider.LOCAL)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        return issueTokens(user);
    }

    // 토큰 재발급
    @Transactional
    public TokenResponse reissue(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = refreshTokenRepository.findUserIdByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.deleteByToken(refreshToken);

        return issueTokens(user);
    }

    //토큰 발급
    private TokenResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(refreshToken, user.getId(), jwtTokenProvider.getRefreshExpiration());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
