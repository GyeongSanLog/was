package yu.spring.gyeongsanlog.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;
import yu.spring.gyeongsanlog.common.jwt.JwtTokenProvider;
import yu.spring.gyeongsanlog.common.jwt.RefreshTokenRepository;
import yu.spring.gyeongsanlog.user.config.KakaoOauthClient;
import yu.spring.gyeongsanlog.user.config.dto.KakaoProfileResponse;
import yu.spring.gyeongsanlog.user.domain.Provider;
import yu.spring.gyeongsanlog.user.domain.User;
import yu.spring.gyeongsanlog.user.dto.KakaoLoginRequest;
import yu.spring.gyeongsanlog.user.dto.MemberLoginRequest;
import yu.spring.gyeongsanlog.user.dto.RefreshRequest;
import yu.spring.gyeongsanlog.user.dto.SignUpRequest;
import yu.spring.gyeongsanlog.user.dto.TokenResponse;
import yu.spring.gyeongsanlog.user.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KakaoOauthClient kakaoOauthClient;
    private final EmailVerificationService emailVerificationService;

    //회원가입
    @Transactional
    public TokenResponse register(SignUpRequest request) {
        if (userRepository.existsByEmailAndProvider(request.getEmail(), Provider.LOCAL)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        // 메일로 받은 코드 검증을 먼저 통과해야 가입할 수 있다
        if (!emailVerificationService.isVerified(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(Provider.LOCAL)
                .build();

        userRepository.save(user);
        emailVerificationService.clearVerified(request.getEmail());

        return issueTokens(user);
    }

    // 시스템 로그인
    @Transactional(readOnly = true)
    public TokenResponse login(MemberLoginRequest request) {
        User user = userRepository.findByEmailAndProvider(request.getEmail(), Provider.LOCAL)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        return issueTokens(user);
    }

    // 카카오 로그인. 처음이면 가입시키고, 이미 있으면 그대로 로그인시킨다.
    @Transactional
    public TokenResponse kakaoLogin(KakaoLoginRequest request) {
        String kakaoAccessToken = kakaoOauthClient.getAccessToken(request.getAuthCode(), request.getRedirectUrl());
        KakaoProfileResponse profile = kakaoOauthClient.getProfile(kakaoAccessToken);

        String providerId = String.valueOf(profile.getId());
        User user = userRepository.findByProviderAndProviderId(Provider.KAKAO, providerId)
                .orElseGet(() -> registerKakaoUser(providerId, profile));

        // 탈퇴해도 provider/providerId는 남겨두므로, 여기서 막지 않으면 재로그인으로 계정이 되살아난다
        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.WITHDRAWN_USER);
        }

        return issueTokens(user);
    }

    /*
     동의항목 검수 전이거나 사용자가 거부하면 이메일/닉네임이 안 넘어올 수 있다.
     email은 not null이라 카카오 회원번호 기반 임시값을 넣고, 닉네임은 중복될 수 있어 뒤에 랜덤 문자열을 붙인다.
     */
    private User registerKakaoUser(String providerId, KakaoProfileResponse profile) {
        String email = profile.getEmailOrNull();
        if (email == null || email.isBlank()) {
            email = "kakao_" + providerId + "@social.local";
        }

        String nickname = profile.getNicknameOrNull();
        if (nickname == null || nickname.isBlank()) {
            nickname = "여행자";
        }

        User user = User.builder()
                .email(email)
                .nickname(generateUniqueNickname(nickname))
                .name(nickname)
                .profileImageUrl(profile.getProfileImageUrlOrNull())
                .provider(Provider.KAKAO)
                .providerId(providerId)
                .build();

        return userRepository.save(user);
    }

    private String generateUniqueNickname(String base) {
        if (!userRepository.existsByNickname(base)) {
            return base;
        }
        String candidate;
        do {
            candidate = base + "_" + UUID.randomUUID().toString().substring(0, 6);
        } while (userRepository.existsByNickname(candidate));
        return candidate;
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

        // 탈퇴 후에도 행이 남아있어 findById는 성공하므로, 만료 전 refresh token으로 재발급되는 걸 막는다
        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.WITHDRAWN_USER);
        }

        return issueTokens(user);
    }

    // 로그아웃 (해당 refresh token 세션만 만료)
    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenRepository.deleteByToken(request.getRefreshToken());
    }

    // 닉네임 중복 확인
    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
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
