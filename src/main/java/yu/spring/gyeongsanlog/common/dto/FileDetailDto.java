package yu.spring.gyeongsanlog.common.dto;

import lombok.Builder;
import lombok.Getter;
import yu.spring.gyeongsanlog.common.domain.FileType;

@Getter
@Builder
public class FileDetailDto {
    private String originalFileName;
    private String key;
    private String contentType;
    private long fileSize;
    private FileType fileType;
}
