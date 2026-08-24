package yu.spring.gyeongsanlog.group.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yu.spring.gyeongsanlog.group.domain.TravelGroup;
import yu.spring.gyeongsanlog.group.repository.TravelGroupRepository;

// GroupVideoMergeService에서 self-invocation으로 @Transactional이 무시되는 것을 막기 위해 별도 빈으로 분리
@Service
@RequiredArgsConstructor
public class GroupMergeStatusUpdater {

    private final TravelGroupRepository travelGroupRepository;

    @Transactional
    public void markProcessing(Long groupId) {
        travelGroupRepository.findById(groupId).ifPresent(TravelGroup::startMerging);
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
