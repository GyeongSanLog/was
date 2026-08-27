package yu.spring.gyeongsanlog.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;
import yu.spring.gyeongsanlog.common.dto.FileDetailDto;
import yu.spring.gyeongsanlog.common.util.S3Uploader;
import yu.spring.gyeongsanlog.user.domain.User;
import yu.spring.gyeongsanlog.user.dto.ChangePasswordRequest;
import yu.spring.gyeongsanlog.user.dto.MemberProfileResponse;
import yu.spring.gyeongsanlog.user.dto.UpdateProfileRequest;
import yu.spring.gyeongsanlog.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;

    // 회원 정보 조회
    @Transactional(readOnly = true)
    public MemberProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return MemberProfileResponse.from(user);
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    // 회원 정보 수정
    @Transactional
    public MemberProfileResponse updateProfile(Long userId, UpdateProfileRequest request, MultipartFile profileImage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.getNickname().equals(request.getNickname())
                && userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        user.changeNickname(request.getNickname());
        user.changeName(request.getName());

        if (request.isResetProfileImage()) {
            user.changeProfileImageUrl(null);
        } else if (profileImage != null && !profileImage.isEmpty()) {
            FileDetailDto meta = s3Uploader.makeMetaData(profileImage, "PROFILE_IMAGE");
            s3Uploader.uploadFile(meta.getKey(), profileImage);
            user.changeProfileImageUrl(s3Uploader.getPublicUrl(meta.getKey()));
        }

        return MemberProfileResponse.from(user);
    }

    // 푸시 알림용 기기 토큰 등록/갱신.
    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateFcmToken(fcmToken);
    }
}
