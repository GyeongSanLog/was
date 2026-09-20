package yu.spring.gyeongsanlog.group.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;

@Service
public class GroupVideoMergeRetryService {
    private final GroupMergeStatusUpdater statusUpdater;
    private final GroupVideoMergeService mergeService;
    private final TaskExecutor executor;

    public GroupVideoMergeRetryService(GroupMergeStatusUpdater statusUpdater, GroupVideoMergeService mergeService,
                                      @Qualifier("groupVideoMergeExecutor") TaskExecutor executor) {
        this.statusUpdater = statusUpdater;
        this.mergeService = mergeService;
        this.executor = executor;
    }

    public void retry(Long userId, Long groupId) {
        statusUpdater.prepareRetry(userId, groupId);
        try {
            executor.execute(() -> mergeService.mergeClaimedGroupVideo(groupId));
        } catch (TaskRejectedException e) {
            statusUpdater.markFailed(groupId);
            throw new BusinessException(ErrorCode.GROUP_MERGE_BUSY);
        }
    }
}
