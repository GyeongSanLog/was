package yu.spring.gyeongsanlog.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.domain.FileRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3Scheduler {

    private final FileRepository fileRepository;
    private final S3Uploader s3Uploader;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void deleteFiles() {
        log.info("Deleting files start");
        fileRepository.findAllByDeletedTrue()
                .forEach(file -> {
                    try {
                        s3Uploader.deleteFile(file.getFileKey());
                        fileRepository.delete(file);
                    } catch (BusinessException e) {
                        log.error("scheduler deleteFile s3 error key: {}", file.getFileKey());
                    } catch (Exception e) {
                        log.error("Scheduler deleteFile database error: {}", file.getFileKey(), e);
                    }
                });
        log.info("Deleting files end");
    }
}
