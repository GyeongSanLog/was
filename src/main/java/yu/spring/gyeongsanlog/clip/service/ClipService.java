package yu.spring.gyeongsanlog.clip.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import yu.spring.gyeongsanlog.clip.domain.Clip;
import yu.spring.gyeongsanlog.clip.dto.ClipResponse;
import yu.spring.gyeongsanlog.clip.dto.UploadClipRequest;
import yu.spring.gyeongsanlog.clip.repository.ClipRepository;
import yu.spring.gyeongsanlog.common.dto.FileDetailDto;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;
import yu.spring.gyeongsanlog.common.util.S3Uploader;
import yu.spring.gyeongsanlog.group.domain.TravelGroup;
import yu.spring.gyeongsanlog.group.repository.GroupMemberRepository;
import yu.spring.gyeongsanlog.group.repository.TravelGroupRepository;
import yu.spring.gyeongsanlog.user.repository.UserRepository;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClipService {

    private final ClipRepository clipRepository;
    private final TravelGroupRepository travelGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public ClipResponse uploadClip(Long userId, Long groupId, UploadClipRequest request, MultipartFile video) {
        TravelGroup group = travelGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        // 그룹 멤버가 아니면 그룹 존재 여부 자체를 숨기기 위해 404로 응답
        if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), userId)) {
            throw new BusinessException(ErrorCode.GROUP_NOT_FOUND);
        }

        if (request.getCapturedAt().isBefore(group.getStartAt())) {
            throw new BusinessException(ErrorCode.INVALID_CAPTURED_AT);
        }

        int slotIndex = (int) Duration.between(group.getStartAt(), request.getCapturedAt()).toHours();

        if (clipRepository.existsByGroupIdAndUserIdAndSlotIndex(group.getId(), userId, slotIndex)) {
            throw new BusinessException(ErrorCode.DUPLICATE_CLIP_SLOT);
        }

        FileDetailDto meta = s3Uploader.makeMetaData(video, "CLIP_VIDEO");
        s3Uploader.uploadFile(meta.getKey(), video);

        Clip clip = Clip.builder()
                .group(group)
                .user(userRepository.getReferenceById(userId))
                .videoUrl(s3Uploader.getPublicUrl(meta.getKey()))
                .comment(request.getComment())
                .slotIndex(slotIndex)
                .capturedAt(request.getCapturedAt())
                .build();
        clipRepository.save(clip);

        return ClipResponse.from(clip);
    }

    // 비멤버에게는 그룹 존재 여부 자체를 숨기기 위해 404로 응답
    @Transactional(readOnly = true)
    public List<ClipResponse> getClipFeed(Long userId, Long groupId) {
        if (!travelGroupRepository.existsById(groupId)) {
            throw new BusinessException(ErrorCode.GROUP_NOT_FOUND);
        }

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new BusinessException(ErrorCode.GROUP_NOT_FOUND);
        }

        return clipRepository.findByGroupIdWithUser(groupId).stream()
                .map(ClipResponse::from)
                .toList();
    }
}
