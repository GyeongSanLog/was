package yu.spring.gyeongsanlog.group.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yu.spring.gyeongsanlog.group.domain.TravelGroup;
import yu.spring.gyeongsanlog.group.domain.MergeStatus;
import yu.spring.gyeongsanlog.group.repository.GroupMemberRepository;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;

import java.time.LocalDateTime;
import yu.spring.gyeongsanlog.group.repository.TravelGroupRepository;

// GroupVideoMergeService에서 self-invocation으로 @Transactional이 무시되는 것을 막기 위해 별도 빈으로 분리
@Service
@RequiredArgsConstructor
public class GroupMergeStatusUpdater {

    private final TravelGroupRepository travelGroupRepository;

    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public boolean markProcessing(Long groupId) {
        TravelGroup group = travelGroupRepository.findByIdForUpdate(groupId).orElse(null);
        if (group == null || group.getMergeStatus() != MergeStatus.NOT_STARTED) {
            return false;
        }
        group.startMerging();
        return true;
    }

    @Transactional
    public void prepareRetry(Long userId, Long groupId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new BusinessException(ErrorCode.GROUP_NOT_FOUND);
        }
        // 상태 확인과 변경을 잠금 안에서 처리해 중복 요청을 막는다.
        TravelGroup group = travelGroupRepository.findByIdForUpdate(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));
        if (group.getMergeStatus() != MergeStatus.FAILED || !group.getEndAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.GROUP_MERGE_RETRY_NOT_ALLOWED);
        }
        group.startMerging();
    }

    @Transactional
    public void markDone(Long groupId, String mergedVideoUrl) {
        travelGroupRepository.findById(groupId).ifPresent(group -> group.completeMerge(mergedVideoUrl));
    }

    @Transactional
    public void markFailed(Long groupId) {
        travelGroupRepository.findById(groupId).ifPresent(TravelGroup::failMerge);
    }
}
